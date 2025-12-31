package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.json.Callbacks;

/// A Climber is a strategy for hashing a JSON tree during its construction
sealed interface Climber extends Callbacks permits AbstractClimber {
}
