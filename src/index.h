#ifndef INDEX_H_
#define INDEX_H_

#include <string>
#include <atomic>


namespace microdb {
	
	class Index : public Environment {
		protected:
		const std::string mName;
		std::atomic<uint64_t> mSequence;
		std::unique_ptr< ViewQuery > mQuery;
		
		public:
		Index(const std::string& name);
		
		void setQuery(std::unique_ptr< ViewQuery >& ptr) {
			mQuery = std::move(ptr);
		}
		
		const std::string getName() {
			return mName;
		}
		
		bool operator< (const Index& o) {
			return mName.compare(o.mName) < 0;
		}
		
	};
	
	static Index& getPrimaryIndex();
	
	
}

#endif // INDEX_H_