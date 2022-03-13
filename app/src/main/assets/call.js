let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

// localVideo.style.opacity = 0
// remoteVideo.style.opacity = 0

localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer
function init(userId) {
    peer = new Peer(userId, {
       
        port: 443,
        path: '/'
    })

    peer.on('open', () => {
        //we will add this when we eill integrate with android
        // Android.onPeerConnected()
    })

    listen()
}

function WebCamStart(){
    navigator.mediaDevices.getUserMedia({video:true,audio:true})
        .then(function (stream){
            localVideo.srcObject = stream;
        })
        .catch(function (error){
            console.log("something went wrong");
        })
}



let localStream
function listen() {
    peer.on('call', (call) => {

        // navigator.getUserMedia({
        //     audio: true, 
        //     video: true
        // }, (stream) => {
        //     localVideo.srcObject = stream
        //     localStream = stream

        //     call.answer(stream)
        //     call.on('stream', (remoteStream) => {
        //         remoteVideo.srcObject = remoteStream

        //         remoteVideo.className = "primary-video"
        //         localVideo.className = "secondary-video"

        //     })

        // })

        navigator.mediaDevices.getUserMedia({video:true,audio:true})
        .then(function (stream){
            
            localVideo.srcObject = stream
            localStream = stream

            call.answer(stream)
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream

                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"

            })

        })
        .catch(function (error){
            console.log("something went wrong");
        })


        
    })
}

function startCall(otherUserId) {
    navigator.mediaDevices.getUserMedia({video:true,audio:true})
        .then(function (stream){
            
            localVideo.srcObject = stream
            localStream = stream

            const call = peer.call(otherUserId, stream)
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream

                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"
            })

        })
        .catch(function (error){
            console.log("something went wrong");
        })
    // navigator.mediaDevices.getUserMedia({
    //     audio: true,
    //     video: true
    // }, (stream) => {

    //     localVideo.srcObject = stream
    //     localStream = stream

    //     const call = peer.call(otherUserId, stream)
    //     call.on('stream', (remoteStream) => {
    //         remoteVideo.srcObject = remoteStream

    //         remoteVideo.className = "primary-video"
    //         localVideo.className = "secondary-video"
    //     })

    // }, function (){console.warn("Error getting stream from getUserMedia")})
}

function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
} 

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
} 