package com.tlab.erik_spectre.tootsigymmb.Model

import android.content.Context
import com.tlab.erik_spectre.tootsigymmb.Utilities.DataParser
import com.tlab.erik_spectre.tootsigymmb.Utilities.HoldsCanvas
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*
import kotlin.concurrent.schedule

class MQTT(private val context: Context,
           private val address: String,
           private val port: String,
           private val username: String,
           private val password: String) {

    //private val clientId = MqttClient.generateClientId()

    private lateinit var client: MqttAndroidClient
    private lateinit var connectionOptions: MqttConnectOptions

    private val mqttTimer = Timer("mqtt", true)

    var initialized = false

    fun init() {
        client = MqttAndroidClient(context, "tcp://$address:$port", MqttClient.generateClientId())
        client.setCallback(MQTTConnectionCallback)
        connectionOptions = MqttConnectOptions()
        connectionOptions.userName = username
        //connectionOptions.isAutomaticReconnect = true
        connectionOptions.password = password.toCharArray()
        startConnection()
    }

    private object MQTTConnectionCallback: MqttCallbackExtended {
        override fun connectComplete(b: Boolean, s: String) {
            println("Connect complete, $s")
        }

        override fun connectionLost(throwable: Throwable) {
            println("Connect lost")
        }

        @Throws(Exception::class)
        override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
            println("messageArrived, $mqttMessage")
            DataParser.parseMQTTResponseData(mqttMessage.toString())
        }

        override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
            println("deliveryComplete")
        }
    }

    fun checkConnection() {
        println("Connected: ${client.isConnected}")
        if (!client.isConnected) reconnect()
    }

    private fun startConnection()
    {
        try {
            val token = client.connect(connectionOptions)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    println("onSuccess")
                    subscribeToTopic()
                    initialized = true
                    }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    println("onFailure")
                }
            }
        } catch (e: MqttException) {
            println("exception: ${e.message}")
        }
    }

    private fun subscribeToTopic() {
        try {
            client.subscribe("moon_resp", 0)
            println("Success subscribe")
        } catch (e: MqttException) {
            println("Failed subscribe")
        }

    }

    private fun publishToTopic(message: String) {
        try {
            val publish = client.publish("moon", message.toByteArray(), 1, false)
            println("Published: ${publish.message}")
        } catch (e: MqttException) {
            println(e.message)
        }
    }

    fun sendData(data: String) {
        checkConnection()
        publishToTopic(data)
    }

    private fun reconnect() {
        startConnection()
        waitForReconnect()
    }

    private fun waitForReconnect(counter: Int = 0) {
        if (client.isConnected) {
            println ("Reconnected in ${counter*5} ms")
        } else {
            mqttTimer.schedule(5) {
                if (counter < 400) waitForReconnect(counter + 1)
            }
        }

        if (counter >= 400) println("Failed to reconnect")
    }
}