package com.pickle.punktual.position

import com.pickle.punktual.firebase.FirebaseManager
import com.pickle.punktual.user.UserStorage
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.util.*



fun Routing.position() {
    post("/position/register/{type}") {
        try {

            val position = call.receive<Position>()
            val type = LocationType.valueOf(call.parameters["type"] ?: "UNKNOWN")
            val shouldNotifyOther = true
            call.request.queryParameters["userId"]?.let { userId ->
                UserStorage
                    .userList
                    .find {
                        it.id == UUID.fromString(userId)
                    }?.let { currentUser ->
                        currentUser.addPosition(position, type)
                        if(shouldNotifyOther) {
                            FirebaseManager.sendNotificationToTeam(currentUser, UserStorage.userList, type)
                        }
                        //Todo: change return here
                        call.respond(HttpStatusCode.Accepted, "Notifications has been sent to all the team :)")
                    }?: run {
                        call.respond(HttpStatusCode.NotFound, "User not exists")
                    }
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, "Missing parameter(s)")
            }
        } catch (exception: Exception){
            call.respond(HttpStatusCode.BadRequest, exception)
        }
    }
}
