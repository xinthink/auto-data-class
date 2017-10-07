package com.fivemiles.auto.dataclass

/**
 * Demonstrating supported property types.
 */
@DataClass interface PropTypes {
    val s: String
    val i: Int
    val sh: Short
    val b: Byte
    val f: Float
    val d: Double
    val z: Boolean
    val ns: String?
    val ls: List<String>
//    val lns: List<String?>  // nullable type argument unsupported
    val msi: Map<String, Int>
    val ss: Set<String>
    val lmsi: List<Map<String, Int>>
}
