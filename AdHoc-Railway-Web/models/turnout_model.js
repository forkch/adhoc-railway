var mongoose = require('mongoose');
var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.TurnoutSchema = new Schema({
	number: {type: Number, index: true},
	bus1: Number,
	bus2: Number,
	address1: Number,
	address2: Number,
	address1switched: Boolean,
	address2switched: Boolean,
	type: String,
	defaultState: String,
	orientation: String,
	description: String,
	group:  ObjectId
});

exports.TurnoutGroupSchema = new Schema({
	name: String,
	turnouts : [ObjectId]
});

exports.RouteSchema = new Schema({
	name: String,
	routedTurnout : {
		turnoutId: ObjectId,
		state: String
	}
})
exports.RouteGroupSchema = new Schema({
	name: String,
	routes : [ObjectId]
})

exports.TurnoutModel = mongoose.model('Turnout', exports.TurnoutSchema);
exports.TurnoutGroupModel = mongoose.model('TurnoutGroup', exports.TurnoutGroupSchema);
exports.RouteModel = mongoose.model('Route', exports.RouteSchema);
exports.RouteGroupModel = mongoose.model('RouteGroup', exports.RouteGroupSchema);