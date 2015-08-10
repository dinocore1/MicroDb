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
const byte Optimized_Size = '#';
const byte Optimized_Type = '$';

} //namespace ubjson
} //namespace microdb

#endif /* UBIJSONHELPER_H_ */
