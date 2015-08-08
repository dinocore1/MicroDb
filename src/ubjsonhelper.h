#ifndef UBIJSONHELPER_H_
#define UBIJSONHELPER_H_


namespace microdb {
namespace ubjson {

const byte Null = 'Z';
const byte True = 'T';
const byte False = 'F';
const byte Char = 'C';
const byte Int8 = 'i';
const byte Uint8 = 'U';
const byte Int16 = 'I';
const byte Int32 = 'l';
const byte Int64 = 'L';
const byte Float32 = 'd';
const byte Float64 = 'D';
const byte String = 'S';
const byte Array_Start = '[';
const byte Array_End = ']';
const byte Object_Start = '{';
const byte Object_End = '}';


enum class Marker : byte {
    Invalid = '\0',

    Null    = 'Z',
    No_Op   = 'N',
    Char    = 'C',
    True    = 'T',
    False   = 'F',

    Int8    = 'i',
    Uint8   = 'U',
    Int16   = 'I',
    Int32   = 'l',
    Int64   = 'L',
    Float32 = 'd',
    Float64 = 'D',
    HighPrecision = 'H',

    String  = 'S',
    Binary  = 'b',  //Extension

    Object_Start   = '{',
    Object_End     = '}',
    Array_Start    = '[',
    Array_End      = ']',

    Array,
    Object,

    Optimized_Type  = '$',
    Optimized_Count = '#'
};

constexpr bool operator == (byte b, Marker m) { return b == static_cast<byte>(m); }

} //namespace ubjson
} //namespace microdb

#endif /* UBIJSONHELPER_H_ */
