var restify = require('restify');
var socketHandler = require('./socketio/socket.js');
var locomotiveApi = require('./rest/locomotiveRESTApi');
var turnoutApi = require('./rest/turnoutRESTApi');
var mongoose = require('mongoose');

var mdns = require('mdns');

var Logger = require('bunyan');
var log = new Logger({
    name: 'helloapi',
    streams: [
        {
            stream: process.stdout,
            level: 'debug'
        },
        {
            path: 'hello.log',
            level: 'trace'
        }
    ],
    serializers: restify.bunyan.serializers
});


var server = restify.createServer({
    name: 'AdHoc-Railway-Server',
    log: log   // Pass our logger to restify.
});
server.use(restify.bodyParser());
server.use(restify.CORS());
server.use(restify.fullResponse());
server.pre(function (request, response, next) {
    request.log.info({req: request}, 'start');        // (1)
    return next();
});
server.on('after', function (req, res, route) {
    req.log.info({res: res}, "finished");             // (3)
});

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
turnoutApi.init(server, sendDataToWebsocketClients);

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

server.listen(3000, function () {
    console.log('%s listening at %s', server.name, server.url);
});

