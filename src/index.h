#ifndef INDEX_H_
#define INDEX_H_

#include <string>
#include <atomic>


namespace microdb {
	
	typedef std::function<void(Value& key, Value& value)> emitCallback;
	
	
	class Index : public Environment, public Serializable {
		private:
		std::string mName;
		
		protected:
		std::atomic<uint64_t> mSequence;
		std::unique_ptr< ViewQuery > mQuery;
		
		void emit(Value& retval, const std::vector< Selector* >& args, emitCallback);
		
		public:
		Index() {};
		Index(const std::string& name);
		virtual ~Index() {}
		
		const std::string getName() const;
		void setQuery(std::unique_ptr< ViewQuery > ptr);
		
		void index(Value&, emitCallback);
		
		//Serializable API
		Value toValue();
		void fromValue(const Value&);
	};
	
	static Index& getPrimaryIndex();
	
	
}

#endif // INDEX_H_