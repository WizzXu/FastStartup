package io.github.fast_startup.transformer

import com.didiglobal.booster.kotlinx.file
import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.*
import com.didiglobal.booster.transform.util.transform
import com.google.auto.service.AutoService
import jdk.internal.org.objectweb.asm.Opcodes.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import java.io.PrintWriter

/**
 * AOP插桩自动添加Startup
 */
@AutoService(ClassTransformer::class)
class FastStartupTransformer : ClassTransformer {

    private val targetClassName = "io.github.fast_startup.FastStartup"
    private val annotationClassName = "Lio/github/fast_startup/annocation/AFastStartup;"

    private lateinit var logger: PrintWriter

    override val name: String = Build.ARTIFACT

    override fun onPreTransform(context: TransformContext) {
        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        val targetClassList = mutableListOf<ClassNode>()
        if (klass.className == targetClassName) {
            val list = context.compileClasspath
            list.forEach { it1 ->
                it1.transform(context.buildDir.file("transforms", it1.name)) { bytecode ->
                    ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
                        ClassNode().also { k ->
                            ClassReader(bytecode).accept(k, 0)
                            k.visibleAnnotations?.forEach {
                                if (it.desc == annotationClassName) {
                                    targetClassList.add(k)
                                }
                            }

                        }.accept(writer)
                    }.toByteArray()
                    bytecode
                }
            }

            klass.methods.forEach { method ->
                if ("${method.name}${method.desc}" == "initAopStartup()V") {
                    method.instructions?.findAll(RETURN, ATHROW, IRETURN)?.forEach { node ->
                        targetClassList.forEach {
                            method.instructions?.insertBefore(node, getNode(it.name))
                        }
                    }
                }
            }
        }
        return klass
    }

    private fun getNode(className: String): InsnList {
        return with(InsnList()) {
            add(
                FieldInsnNode(
                    GETSTATIC, targetClassName.replace(".", "/"), "aopStartups", "Ljava/util/List;"
                )
            )
            add(TypeInsnNode(NEW, className))
            add(InsnNode(DUP))
            add(MethodInsnNode(INVOKESPECIAL, className, "<init>", "()V", false))
            add(MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true))
            add(InsnNode(POP))
            this
        }
    }
}