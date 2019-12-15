
public enum BinarySize 
{
	x1(0),
	x2(1),
	x4(2),
	x8(3),
	x16(4),
	x32(5),
	x64(6),
	x128(7),
	x256(8),
	x512(9),
	x1024(10),
	x2048(11),
	x4096(12),
	x8192(13),
	x16384(14),
	x32768(15),
	x65536(16);
	
	public int size;
	public int max;
	public int bits;
	
	private BinarySize(int bits)
	{
		this.bits = bits;
		size = 1 << bits;
		max = size - 1;
	}
	
	public static BinarySize match(int size)
	{
		switch(size)
		{
		case 1: return x1;
		case 2: return x2;
		case 4: return x4;
		case 8: return x8;
		case 16: return x16;
		case 32: return x32;
		case 64: return x64;
		case 128: return x128;
		case 256: return x256;
		case 512: return x512;
		case 1024: return x1024;
		case 2048: return x2048;
		case 4096: return x4096;
		case 8192: return x8192;
		case 16384: return x16384;
		case 32768: return x32768;
		case 65536: return x65536;
		default: 
			Application.throwError("Cannot match size to binary amount!", "NO MATCH");
			return null;
		}
	}
}
