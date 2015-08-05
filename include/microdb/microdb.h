
#ifndef MICRODB_DB_H_
#define MICRODB_DB_H_

#include <stdio.h>

namespace microdb {

    static const int kMajorVersion = 1;
    static const int kMinorVersion = 0;

    typedef uint8_t byte;

    class InputStream {
    public:
      virtual ~InputStream() = 0;

      /**
      * Reads up to max bytes of data from the stream
      * into the buf.
      *
      * returns the total number of bytes read into the buffer
      */
      virtual int Read(byte* buf, const size_t max) = 0;
    };

    class OutputStream {
    public:
      virtual ~OutputStream() = 0;

      /**
      * Writes {@code len} bytes from the buf into the stream
      */
      virtual void Write(byte* buf, const size_t len) = 0;
    };



    class Iterator {
    public:
      Iterator();
      virtual ~Iterator();

      virtual void SeekToFirst() = 0;
      virtual void SeekToLast() = 0;

      virtual bool Valid() const = 0;
      virtual void Next() = 0;
      virtual void Prev() = 0;

      virtual std::string& Key() const = 0;
      virtual std::string& Value() const = 0;
    };

    class Checkpoint {
    public:
      Checkpoint();
      virtual ~Checkpoint();


    };

    class DB {
    public:
        static Status Open(const std::string& dbdirpath, DB** dbptr);

        virtual ~DB();

        virtual Status AddView(const std::string& viewName, const std::string& mapQuery) = 0;
        virtual Status DeleteView(const std::string& viewName) = 0;

        virtual Status Insert(const std::string& value, std::string* key = nullptr) = 0;
        virtual Status Update(const std::string& key, const std::string& value) = 0;
        virtual Status Delete(const std::string& key) = 0;

        //virtual Checkpoint GetCheckpoint() = 0;
        //virtual void Push(const Checkpoint& since, OutputStream& out) = 0;
        //virtual void Pull(InputStream& in) = 0;
    };
}

#endif
