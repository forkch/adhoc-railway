/*
 * Serve content over a socket
 */

var data = {
    "turnouts":[
        {
            "id":"1",
            "number":"1",
            "bus1":"1",
            "address1":"2",
            "bus2":"",
            "address2":"",
            "type":"default"

        },
        {
            "id":"2",
            "number":"2",
            "bus1":"1",
            "address1":"4",
            "bus2":"",
            "address2":"",
            "type":"doublecross"

        },
        {
            "id":"3",
            "number":"4",
            "bus1":"1",
            "address1":"7",
            "bus2":"1",
            "address2":"8",
            "type":"threeway"
        }
    ],
    "locomotives":[
        {
            "id":"1",
            "name":"ICN",
            "bus":"1",
            "address":"5"
        },
        {
            "id":"2",
            "name":"ascom",
            "bus":"1",
            "address":"24"
        }
    ]
};

module.exports = function (socket) {
    socket.emit('init', data);

    socket.on('turnout:add', function (turnout, fn) {
        if (turnout.number >= 1) {
            console.log('adding new turnout ' + JSON.stringify(turnout));
            data.turnouts.push(turnout);
            socket.broadcast.emit('turnout:added', turnout);
            fn(true, '');
        } else {
            fn(false, 'number should be greater 0');
        }
    });
    socket.on('turnouts:getAll', function (dummy, fn) {
        fn(data.turnouts);
    });

    socket.on('turnouts:getFromId', function (id, fn) {
        fn();
    });

    socket.on('locomotives:getAll', function (dummy, fn) {
        fn(data.locomotives);
    });

};
