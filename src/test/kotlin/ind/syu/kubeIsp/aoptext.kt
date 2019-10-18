package ind.syu.kubeIsp

import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

interface Interceptor{
    fun before():Boolean
    fun around(invocation: Invocation):Any?
    fun after()
    fun afterReturing()
    fun afterThrowing()
    fun useAround():Boolean
}

class Invocation(
        var target:Any,
        var method:Method,
        var params :Array<Any>
){
    fun proceed():Any? {
        var obj =method.invoke(target,*params)
        return obj
    }
}

class MyInterceptor:Interceptor{
    override fun before(): Boolean {
        println("before")
        return false
    }

    override fun around(invocation: Invocation): Any? {
        println("around before")
        val obj = invocation.proceed()
        println("around after")
        return obj
    }

    override fun after() {
        println("after")

    }

    override fun afterReturing() {
        println("afterReturing")
    }

    override fun afterThrowing() {
        println("afterThrowing")
    }

    override fun useAround(): Boolean  {
        return true
    }

}

class ProxyBean : InvocationHandler{
    companion object{
        fun getProxyBean(target: Any,interceptor: Interceptor):Any?{
            var proxyBean = ProxyBean()
            proxyBean.target = target
            proxyBean.interceptor = interceptor
            var proxy = Proxy.newProxyInstance(target::class.java.classLoader,
                    target::class.java.interfaces,
                    proxyBean)
            return proxy
        }
    }

    private var target:Any?=null
    private var interceptor:Interceptor?=null

    override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
        var exceptionFlag = false
        var invocation = Invocation(target!!,method,args)
        var retObj:Any?=null
        try {
            if(this.interceptor!!.before()){
                retObj = this.interceptor!!.around(invocation)
            }else{
                retObj = method.invoke(target,*args)
            }
        }catch (e:Exception){
            e.printStackTrace()
            exceptionFlag=true
        }
        this.interceptor!!.after()
        if(exceptionFlag){
            this.interceptor!!.afterThrowing()
        }else{
            this.interceptor!!.afterReturing()
            return retObj
        }
        return null
    }

}

interface SayHi{
    fun hi(name:String):String
}

class SayHiImpl:SayHi{
    override fun hi(name:String):String {
        println("hi!!!!!!$name")
        return name
    }

}

fun main(args: Array<String>) {
    var hi = SayHiImpl()
    var hiproxy = ProxyBean.getProxyBean(hi,MyInterceptor()) as SayHi
    hiproxy.hi("kknd")
}