var mongoose = require('mongoose');
var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.TurnoutSchema = new Schema({
    number: {type: Number, index: true},
    bus1: Number,
    bus2: Number,
    address1: Number,
    address2: Number,
    address1Switched: Boolean,
    address2Switched: Boolean,
    type: String,
    defaultState: String,
    orientation: String,
    description: String,
    groupId: ObjectId
});

exports.TurnoutGroupSchema = new Schema({
    name: String,
    turnoutNumberOffset: Number,
    turnoutNumberAmount: Number,
    turnouts: [ObjectId]
});

exports.RouteSchema = new Schema({
    number: {type: Number, index: true},
    name: String,
    orientation: String,
    groupId: ObjectId,
    routedTurnouts: [
        {
            turnoutNumber: Number,
            state: String
        }
    ]
});
exports.RouteGroupSchema = new Schema({
    name: String,
    routes: [ObjectId]
});

exports.TurnoutSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};
exports.TurnoutGroupSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};
exports.RouteSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};
exports.RouteGroupSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};

exports.TurnoutModel = mongoose.model('Turnout', exports.TurnoutSchema);
exports.TurnoutGroupModel = mongoose.model('TurnoutGroup', exports.TurnoutGroupSchema);
exports.RouteModel = mongoose.model('Route', exports.RouteSchema);
exports.RouteGroupModel = mongoose.model('RouteGroup', exports.RouteGroupSchema);
