package ind.syu.kubeIsp.controller


import io.kubernetes.client.Exec
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.KubeConfig
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

import org.springframework.stereotype.Component
import org.yeauty.annotation.*
import org.yeauty.annotation.OnEvent
import org.yeauty.annotation.OnBinary
import org.yeauty.annotation.OnMessage
import org.yeauty.annotation.OnError
import org.yeauty.annotation.OnClose
import kotlin.reflect.jvm.internal.impl.types.checker.ClassicTypeSystemContext.DefaultImpls.getParameter
import org.yeauty.pojo.ParameterMap
import org.yeauty.annotation.OnOpen
import org.yeauty.annotation.ServerEndpoint
import org.yeauty.pojo.Session
import java.io.FileReader
import java.io.IOException
import java.util.concurrent.TimeUnit


@ServerEndpoint(port = 7008,path = "/exec")
@Component
class ExecWebSocket {

    @Value("\${kube-isp.kubeConfigPath}")
    lateinit var kubeConfigPath: String

    companion object{
        private val log: Logger = LoggerFactory.getLogger(ExecWebSocket::class.java)
    }

    @OnOpen
    @Throws(IOException::class)
    fun onOpen(session: Session, headers: HttpHeaders, parameterMap: ParameterMap) {
        log.info("onOpen")
        val namespace = parameterMap.getParameter("ns");
        val name = parameterMap.getParameter("name");
        val container = parameterMap.getParameter("container");

        val client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(FileReader(kubeConfigPath))).build()
        client.httpClient.setReadTimeout(0L, TimeUnit.MILLISECONDS)
        io.kubernetes.client.Configuration.setDefaultApiClient(client)
        val exec = Exec()
        val proc = exec.exec(namespace, name, arrayOf("sh"), container,true, true);
        session.setAttribute("process",proc)
        session.setAttribute("runing",true)

        val input=proc.inputStream
        val sendJob = Thread(
                Runnable {
                    while (session.getAttribute("runing") as Boolean){
                        Thread.sleep(200)
                        var readCount = input.available()
                        if(readCount>0) {
                            val byteArray = ByteArray(readCount)
                            input.read(byteArray)
                            log.info(String(byteArray))
                            //session.sendMessage(TextMessage(String(byteArray)))
                            session.sendBinary(byteArray)
                            log.info("------")
                        }
                    }
                })
        sendJob.start()
        session.setAttribute("sendJob",sendJob)
        log.info("onOpen complete!!!!")

    }

    @OnClose
    @Throws(IOException::class)
    fun onClose(session: Session)  {
        log.info("afterConnectionClosed")
        session.setAttribute("running",false)
        val sendJob = session.getAttribute("sendJob") as Thread
        sendJob.join()

        val proc = session.getAttribute("process") as Process
        proc.destroy()
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        throwable.printStackTrace()
    }

    @OnMessage
    fun onMessage(session: Session, message: String) {
        log.info("handleTextMessage,payload:{}",message)
        val proc = session.getAttribute("process") as Process
        proc.outputStream.write(message.toByteArray())
        log.info("handleTextMessage,compelet")
    }

    @OnBinary
    fun onBinary(session: Session, bytes: ByteArray) {
        for (b in bytes) {
            println(b)
        }
        session.sendBinary(bytes)
    }

    @OnEvent
    fun onEvent(session: Session, evt: Any) {
        if (evt is IdleStateEvent) {
            when (evt.state()) {
                IdleState.READER_IDLE -> println("read idle")
                IdleState.WRITER_IDLE -> println("write idle")
                IdleState.ALL_IDLE -> println("all idle")
                else -> {
                }
            }
        }
    }

}



