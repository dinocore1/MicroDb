#ifndef SERIALIZERHELPER_H_
#define SERIALIZERHELPER_H_

#include <cstdint>
#include "portable_endian.h"

namespace microdb {

  uint16_t toBigEndian16(uint16_t val){
    return htobe16(val);
  }

  uint32_t toBigEndian32(uint32_t val)
  {  return htobe32(val); }

  uint64_t toBigEndian64(uint64_t val)
  {  return htobe64(val); }

  uint32_t toBigEndianFloat32(float val)
  {
      uint32_t rtn;
      std::memcpy(&rtn, &val, sizeof(rtn));
      return toBigEndian32( rtn );
  }

  uint64_t toBigEndianFloat64(double val)
  {
      uint64_t rtn;
      std::memcpy(&rtn, &val, sizeof(rtn));
      return toBigEndian64( rtn );
  }

  uint16_t fromBigEndian16(uint16_t val)
  {  return be16toh(val); }

  uint32_t fromBigEndian32(uint32_t val)
  {  return be32toh(val); }

  uint64_t fromBigEndian64(uint64_t val)
  {  return be64toh(val); }

  float fromBigEndianFloat32(uint32_t val)
  {
      float rtn;
      val = fromBigEndian32(val);
      std::memcpy(&rtn, &val, sizeof(rtn));
      return rtn;
  }

  double fromBigEndianFloat64(uint64_t val)
  {
      double rtn;
      val = fromBigEndian64(val);
      std::memcpy(&rtn, &val, sizeof(rtn));
      return rtn;
  }



  ////////////////////////////////////
  ///
  ///
  ///
  ///////////////////////////////////////


  uint8_t fromBigEndian8(byte* b)
  {
      return *b;
  }

  uint16_t fromBigEndian16(byte* b)
  {
      uint16_t rtn;
      std::memcpy(&rtn, b, 2);
      return fromBigEndian16(rtn);
  }

  uint32_t fromBigEndian32(byte* b)
  {
      uint32_t rtn;
      std::memcpy(&rtn, b, 4);
      return fromBigEndian32(rtn);
  }

  uint64_t fromBigEndian64(byte* b)
  {
      uint64_t rtn;
      std::memcpy(&rtn, b, 8);
      return fromBigEndian64(rtn);
  }

  float fromBigEndianFloat32(byte* b)
  {
      float rtn;
      const uint32_t ans = fromBigEndian32(b);
      std::memcpy(&rtn, &ans, 4);
      return rtn;
  }

  double fromBigEndianFloat64(byte* b)
  {
      double rtn;
      const int64_t ans = fromBigEndian64(b);
      std::memcpy(&rtn, &ans, 8);
      return rtn;
  }

}

#endif /* SERIALIZERHELPER_H_ */
