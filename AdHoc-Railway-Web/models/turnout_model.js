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
	description: String

});
exports.TurnoutGroupSchema = new Schema({
	name: String,
	turnouts : [exports.TurnoutSchema]
});

exports.TurnoutModel = mongoose.model('Turnout', exports.TurnoutSchema);
exports.TurnoutGroupModel = mongoose.model('TurnoutGroup', exports.TurnoutGroupSchema);