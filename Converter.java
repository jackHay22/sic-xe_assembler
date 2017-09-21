
public class Converter {
	//general value and format conversions
	public String byteBin(byte num) {
		String res = String.format("%8s", Integer.toBinaryString(num & 0xFF)).replace(' ', '0');
		return res;
	}
	public String byteBin(int num) {
		if (num == 0) {
			return "00" + Integer.toHexString(num);
		}
		return Integer.toHexString(num).toUpperCase();
	}
	public String charInt(String chars) {
		StringBuilder out = new StringBuilder();
		char[] charArray = chars.toCharArray();
		for (int i = 0;i<charArray.length;i++) {
			out.append(Integer.toHexString((int)charArray[i]).toUpperCase());
		}
		return out.toString();
	}

}
