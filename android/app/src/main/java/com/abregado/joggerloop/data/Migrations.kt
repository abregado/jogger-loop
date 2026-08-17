package com.abregado.joggerloop.data

import org.json.JSONObject

/**
 * Upgrades a raw JSON blob to [CURRENT_SCHEMA_VERSION], one version step at a time.
 * Add a new `if (version < N)` block here for every future schema change - never
 * remove or renumber an existing step, since on-disk data must always be able to
 * walk forward from whatever version it was originally written with.
 */
fun migrate(json: JSONObject): JSONObject {
    // No migrations yet - this is schema version 1, the first ever released.
    // When schema version 2 is introduced, this becomes:
    //   val version = json.optInt("schemaVersion", 1)
    //   if (version < 2) { json.put("someNewField", someDefault) }
    json.put("schemaVersion", CURRENT_SCHEMA_VERSION)
    return json
}
