#ifndef SERIALIZE_H_
#define SERIALIZE_H_

#include <memory>

namespace microdb {

  typedef uint8_t byte;

  class ValueWriter {
  public:
    virtual ~ValueWriter() {};
    virtual void write(const Value&) = 0;
  };

  class ValueReader {
  public:
    virtual ~ValueReader() {};
    virtual bool read(Value&) = 0;
  };

  class InputStream {
  public:
    virtual ~InputStream() {};

    /**
    * Reads up to max bytes of data from the stream
    * into the buf.
    * If max is 0, then no bytes are read and 0 is returned,
    * otherwise this method will block until at least one byte is
    * read and stored into buf or.
    *
    * returns the total number of bytes read into the buffer or -1
    * if EOF is detected.
    */
    virtual int Read(byte* buf, const size_t max) = 0;

    /**
    * Reads max bytes of data from the stream and stores into
    * buf. This method block until exactly max bytes are read or
    * EOF is detected.
    *
    * return true if max bytes were read and stored into buf, false
    * otherwise.
    */
    bool ReadFully(byte* buf, const size_t max) {
      size_t i = 0;
      while(i != max) {
        int bytesRead = Read(&buf[i], max - i);
        if(bytesRead < 0) {
          return false;
        }
        i += bytesRead;
      }
      return true;
    }
  };

  class OutputStream {
  public:
    virtual ~OutputStream() {};

    /**
    * Writes {@code len} bytes from the buf into the stream
    */
    virtual void Write(const void* buf, const size_t len) = 0;
  };
  
  class MemOutputStream : public OutputStream {

    private:
    std::unique_ptr<int8_t[]> mBuffer;
    uint32_t mBufSize;
    uint32_t mWriteIndex;
    
    public:
    MemOutputStream();
    
    void Write(const void* buf, const size_t len);
    
    void GetData(void*& buf, uint32_t& size) const;
  };
  
  class MemInputStream : public InputStream {
    private:
    const byte* mBuffer;
    const uint32_t mBufSize;
    uint32_t mReadIndex;
    
    public:
    MemInputStream(const byte* buf, const uint32_t size);
    
    int Read(byte* buf, const size_t max);
  };

  class UBJSONWriter : public ValueWriter {
  private:
    OutputStream& mOutput;
  public:
    UBJSONWriter(OutputStream&);
    ~UBJSONWriter();

    void write(const Value&);
  };

  class UBJSONReader : public ValueReader {
  private:
    InputStream& mInput;

  public:
    UBJSONReader(InputStream&);
    ~UBJSONReader();

    bool read(Value&);
  };


}

#endif /* SERIALIZE_H_ */
