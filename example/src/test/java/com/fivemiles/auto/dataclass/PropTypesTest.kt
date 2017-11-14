package com.fivemiles.auto.dataclass

/**
 * Demonstrating supported property types.
 */
@DataClass interface PropTypes {
    val s: String
    val ns: String?
    val i: Int
    val ni: Int?
    val sh: Short
    val nsh: Short?
    val b: Byte
    val nb: Byte?
    val f: Float
    val nf: Float?
    val d: Double
    val nd: Double?
    val z: Boolean
    val nz: Boolean?
    val ls: List<String>
//    val lns: List<String?>  // nullable type argument unsupported
    val msi: Map<String, Int>
    val ss: Set<String>
    val lmsi: List<Map<String, Int>>
}
