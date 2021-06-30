package io.github.qzcsfchh.plugin.transformer

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * 关于Javassist用法，参见：<a href='https://ljd1996.github.io/2020/04/23/Javassist%E7%94%A8%E6%B3%95/'>Javassist用法</a>
 */
class InjectUtil {

    static void inject(String path, Project project) {
        File dir = new File(path)
        if (dir.isDirectory()){
            dir.eachFileRecurse {
                if (it.name.endsWith("Activity.class")) {
                    doInject(project, it, path)
                }
            }
        }

    }


    private static void doInject(Project project, File clsFile, String originPath) {
        println("[Inject] DoInject: $clsFile.absolutePath")
        String cls = new File(originPath).relativePath(clsFile).replace('/', '.')
        cls = cls.substring(0, cls.lastIndexOf('.class'))
        println("[Inject] Cls: $cls")

        ClassPool pool = ClassPool.getDefault()
        // 加入当前路径
        pool.appendClassPath(originPath)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage('android.os.Bundle')

        CtClass ctClass = pool.getCtClass(cls)
        // 解冻
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        // 获取方法
        CtMethod ctMethod = ctClass.getDeclaredMethod('onCreate')

        String toastStr = 'android.widget.Toast.makeText(this, "I am the injected code", android.widget.Toast.LENGTH_SHORT).show();'

        // 方法尾插入
        ctMethod.insertAfter(toastStr)
        ctClass.writeFile(originPath)

        // 释放
        ctClass.detach()
    }

}