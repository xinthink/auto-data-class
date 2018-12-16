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
    val msl: Map<String, Long>
    val mss: Map<String, Short>
    val msb: Map<String, Byte>
    val msf: Map<String, Float>
    val msd: Map<String, Double>
    val msz: Map<String, Boolean>
    val ss: Set<String>
    val sl: Set<Long>
    val ssh: Set<Short>
    val sb: Set<Byte>
    val sf: Set<Float>
    val sd: Set<Double>
    val sz: Set<Boolean>
    val lmsi: List<Map<String, Int>>
    val lmsl: List<Map<String, Long>>
    val lmss: List<Map<String, Short>>
    val lmsb: List<Map<String, Byte>>
    val lmsf: List<Map<String, Float>>
    val lmsd: List<Map<String, Double>>
    val lmsz: List<Map<String, Boolean>>
}

class PropTypesTest
