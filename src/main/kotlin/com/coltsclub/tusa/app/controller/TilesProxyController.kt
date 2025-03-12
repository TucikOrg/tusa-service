package com.coltsclub.tusa.app.controller

import java.net.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TilesProxyController {
    @Value("\${app.mapbox.token}")
    private lateinit var token: String

    private val cache: MutableMap<String, ByteArray> = mutableMapOf()

    @GetMapping("api/v1/tiles/{zoom}/{x}/{y}.mvt")
    fun getTile(
        @PathVariable zoom: Int,
        @PathVariable x: Int,
        @PathVariable y: Int
    ): ResponseEntity<ByteArray> {
        // пробуем грузить из кэша
        cache["$zoom/$x/$y"]?.let {
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.mapbox-vector-tile"))
                .body(it)
        }

        val urlStr = "https://api.mapbox.com/v4/mapbox.mapbox-streets-v8,mapbox.mapbox-terrain-v2/$zoom/$x/$y.mvt?access_token=$token"
        val url = URL(urlStr)
        val tileBytes = url.readBytes()

        // сохраняем в кэш
        cache["$zoom/$x/$y"] = tileBytes

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.mapbox-vector-tile"))
            .body(tileBytes)
    }
}