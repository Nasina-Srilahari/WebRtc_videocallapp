let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

localVideo.onplaying = () =>{ localVideo.style.opacity = 1 }
remoteVideo.onplaying = () =>{ remoteVideo.style.opacity = 1 }

let peer
//function init(userId){
//    peer = new Peer(userId,{
//        host:'192.168.43.81',
//        port: 9000,
//        path:'/videocallapp'
//
//    })

function init(userId) {
    var ngrokUrl = 'https://5a9e-2401-4900-4cc1-3a19-5c09-c286-7403-8990.in.ngrok.io';
     peer = new Peer(userId, {
        host: ngrokUrl,
        port: 90, // Update the port if necessary
        path: '/videocallapp'
    });






    peer.on('open', () => {
        Android.onPeerConnected()
    })

    listen()
}

let localStream
function listen(){
    peer.on('call', (call)=>{
        navigator.getUserMedia({
            audio: true,
            video: true

        },(stream)=>{
            localVideo.srcObject = stream
            localStream = stream

            call.answer(stream)
            call.on('stream', (remoteStream)=>{
                remoteVideo.srcObject = remoteStream

                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"
            })
        })
    })
}

function startCall(otherUserId){
    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true

    },(stream)=>{
        localVideo.srcObject = stream
        localStream = stream

        const call = peer.call(otherUserId, stream)
        call.on('stream', (remoteStream)=>{
            remoteVideo.srcObject = remoteStream

            remoteVideo.className = "primary-video"
            localVideo.className = "secondary-video"
        })
    })

    .catch((error) => {
        console.error('Error accessing media devices:', error);
      });

}


function toggleVideo(b){
    if(b=="true"){
        localStream.getVideoTracks()[0].enable = true
    }
    else{
        localStream.getVideoTracks()[0].enable = false
    }
}

function toggleAudio(b){
    if(b=="true"){
        localStream.getAudioTracks()[0].enable = true
    }
    else{
        localStream.getAudioTracks()[0].enable = false
    }
}