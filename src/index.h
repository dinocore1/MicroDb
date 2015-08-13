#ifndef INDEX_H_
#define INDEX_H_

#include <string>
#include <atomic>


namespace microdb {
	
	class Index {
		private:
		const std::string mName;
		std::atomic<uint64_t> mSequence;
		
		public:
		Index(const std::string& name)
		: mName(name) {}
		
		const std::string getName() {
			return mName;
		}
		
		bool operator< (const Index& o) {
			return mName.compare(o.mName) < 0;
		}
		
		ViewQuery mQuery;
		
	};
}

#endif // INDEX_H_