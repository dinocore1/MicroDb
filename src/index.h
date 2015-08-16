#ifndef INDEX_H_
#define INDEX_H_

#include <string>
#include <atomic>


namespace microdb {
	
	class Index : public Environment, public Serializable {
		private:
		std::string mName;
		
		protected:
		std::atomic<uint64_t> mSequence;
		std::unique_ptr< ViewQuery > mQuery;
		Driver* mDriver;
		
		public:
		Index() {};
		Index(const std::string& name);
		virtual ~Index() {}
		
		void setQuery(std::unique_ptr< ViewQuery >& ptr) {
			mQuery = std::move(ptr);
		}
		
		void setDriver(Driver* driver) {
			mDriver = driver;
		}
		
		const std::string getName() {
			return mName;
		}
		
		void index(Value&);
		
		void emit(Value& key, Value& value);
		
		Value toValue();
		void fromValue(const Value&);
		
		
	};
	
	static Index& getPrimaryIndex();
	
	
}

#endif // INDEX_H_