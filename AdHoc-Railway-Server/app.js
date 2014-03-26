var restify = require('restify');
var socketHandler = require('./socketio/socket.js');
var locomotiveApi = require('./rest/locomotiveApi');
var mongoose = require('mongoose');

var mdns = require('mdns');

var server = restify.createServer();
server.use(restify.bodyParser());
server.use(restify.CORS());
server.use(restify.fullResponse());

function reqSerializer(req) {
    return {
        method: req.method,
        url: req.url,
        headers: req.headers
    }
}

server.use(restify.requestLogger({
    properties: {},
    serializers: {req: reqSerializer}
}));

var clients = [];

var io = require('socket.io').listen(server, {origins: '*:*'});

io.sockets.on('connection', function (client) {
    clients.push(client);

    client.on('disconnect', function () {
        clients.splice(clients.indexOf(client), 1);
    });
    return socketHandler(client);
});

function sendDataToWebsocketClients(event, data) {
    io.sockets.clients().forEach(function (socket) {
        socket.emit(event, data);
    });
}
locomotiveApi.init(server, sendDataToWebsocketClients);

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

server.listen(3001, function () {
    console.log('%s listening at %s', server.name, server.url);
});

