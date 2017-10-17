package com.fivemiles.auto.dataclass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@DataClass(generateGsonTypeAdapter = false)
interface TransientData {
    val name: String

    /** Nullable transient prop without a default value */
    val ntp: String?
        @DataProp(isTransient = true) get

    /** Non-nullable transient prop need a default value */
    var tp: String
        @DataProp(isTransient = true,
                defaultValueLiteral = "\"non-nullable transient property\""
        ) get
}

class TransientPropTest {

    @Test fun transientPropInitialized() {
        val data: TransientData = DC_TransientData("Jon")
        assertEquals("Jon", data.name)
        assertNull(data.ntp)
        assertEquals("non-nullable transient property", data.tp)
    }

    @Test fun allowMutableTransientProp() {
        val data: TransientData = DC_TransientData("")
        assertEquals("non-nullable transient property", data.tp)

        data.tp = ""
        assertEquals("", data.tp)
    }
}
