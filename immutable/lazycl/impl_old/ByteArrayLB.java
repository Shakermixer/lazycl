/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

public class ByteArrayLB extends WrapperLB<byte[]>{ //TODO rename to ByteArrayLB

	public ByteArrayLB(byte[] data){
		super(data);
	}
	
	public byte piz(){
		return 3;
	}
	
	public int pize(){
		return 8;
	}
	
	public long bize(){
		return ((long)data.length)<<3; //TODO optimize by caching that?
	}
	
	public byte b(int i){
		return data[i];
	}
	
	public short s(int i){
		return  (short)(((data[i>>1]&0xff)<<8)|((data[(i>>1)+1])&0xff));
	}
	
	public int i(int i){
		int x = i>>2;
		return  (int)(
			((data[x]&0xff)<<24)
			| ((data[x+1]&0xff)<<16)
			| ((data[x+2]&0xff)<<8)
			| (data[x+3]&0xff)
		);
	}
	
	public long j(int i){
		return ((long)i(i>>1))|(long)i(i>>1+1);
	}

}
