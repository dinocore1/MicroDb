#ifndef MicroDB_viewquery_h
#define MicroDB_viewquery_h

#include <rapidjson/rapidjson.h>
#include <rapidjson/document.h>

namespace microdb {
    
    class Selector {
    public:
        virtual rapidjson::Value& select(rapidjson::Value& value) = 0;
    };
    
    class PathStart : public Selector {
    public:
        PathStart() {};
        
        rapidjson::Value& select(rapidjson::Value& value) { return value; };
    };
    
    class MemberSelector : public Selector {
    private:
        const std::string mMemberName;
        Selector* mParent;
        
    public:
        MemberSelector(std::string& memberName, Selector* parent = nullptr)
        : mMemberName(memberName), mParent(parent) {}
        
        rapidjson::Value& select(rapidjson::Value& value) {
            
            
            if(mParent != nullptr){
                value = mParent->select(value);
            }
            
            const char* memberName = mMemberName.c_str();
            if(value.IsObject() && value.HasMember(memberName)){
                return value[memberName];
            }
            
            value.SetNull();
            return value;
        }
    };
    
    class ViewQuery {
    private:
        Selector* mSelector;
    public:
        bool compile(const char* code);
        
        void evaluate(rapidjson::Document& input);
    };
}

#endif