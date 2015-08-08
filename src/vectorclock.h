#ifndef VECTORCLOCK_H_
#define VECTORCLOCK_H_

#include <unordered_map>
#include <string>

namespace microdb {

  class VectorClock {
  private:
    std::unordered_map<std::string, uint64_t> mIdMap;

  public:
    VectorClock();
    VectorClock(const Value&);

    void increment(const std::string& key);
    bool isLessThan(const VectorClock&) const;
    bool operator< (const VectorClock&) const;
    Value toValue() const;

  };

}


#endif /* VECTORCLOCK_H_ */
