package com.github.kjetilv.uplift.json.mame;

import module uplift.json;

/// A Climber is a strategy for hashing and JSON tree during its construction
sealed interface Climber extends Callbacks permits AbstractClimber {
}
