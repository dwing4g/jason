package jason;

import java.nio.charset.StandardCharsets;

public class JasonWriter {
	//@formatter:off
	private static final long DOUBLE_SIGN_MASK        = 0x8000_0000_0000_0000L;
	private static final long DOUBLE_EXP_MASK         = 0x7FF0_0000_0000_0000L;
	private static final long DOUBLE_SIGNIFICAND_MASK = 0x000F_FFFF_FFFF_FFFFL; // low 52-bit
	private static final long DOUBLE_HIDDEN_BIT       = 0x0010_0000_0000_0000L; // 1e52
	private static final int  DOUBLE_SIGNIFICAND_SIZE = 52;
	private static final int  DOUBLE_EXP_SIZE         = 64 - DOUBLE_SIGNIFICAND_SIZE - 2; // 10
	private static final int  DOUBLE_EXP_BIAS         = 0x3FF + DOUBLE_SIGNIFICAND_SIZE;  // 0x433
	//@formatter:on

	private static final byte[] DIGITES_LUT = { // [200]
			'0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', '1',
			'0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '2', '0',
			'2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '3', '0', '3',
			'1', '3', '2', '3', '3', '3', '4', '3', '5', '3', '6', '3', '7', '3', '8', '3', '9', '4', '0', '4', '1',
			'4', '2', '4', '3', '4', '4', '4', '5', '4', '6', '4', '7', '4', '8', '4', '9', '5', '0', '5', '1', '5',
			'2', '5', '3', '5', '4', '5', '5', '5', '6', '5', '7', '5', '8', '5', '9', '6', '0', '6', '1', '6', '2',
			'6', '3', '6', '4', '6', '5', '6', '6', '6', '7', '6', '8', '6', '9', '7', '0', '7', '1', '7', '2', '7',
			'3', '7', '4', '7', '5', '7', '6', '7', '7', '7', '8', '7', '9', '8', '0', '8', '1', '8', '2', '8', '3',
			'8', '4', '8', '5', '8', '6', '8', '7', '8', '8', '8', '9', '9', '0', '9', '1', '9', '2', '9', '3', '9',
			'4', '9', '5', '9', '6', '9', '7', '9', '8', '9', '9' };

	private static final int[] POW10 = { 1, 10, 100, 1000, 1_0000, 10_0000, 100_0000, 1000_0000, 1_0000_0000 };

	private static final long[] CACHED_POWERS_F = { 0xfa8fd5a0_081c0288L, 0xbaaee17f_a23ebf76L, 0x8b16fb20_3055ac76L,
			0xcf42894a_5dce35eaL, 0x9a6bb0aa_55653b2dL, 0xe61acf03_3d1a45dfL, 0xab70fe17_c79ac6caL,
			0xff77b1fc_bebcdc4fL, 0xbe5691ef_416bd60cL, 0x8dd01fad_907ffc3cL, 0xd3515c28_31559a83L,
			0x9d71ac8f_ada6c9b5L, 0xea9c2277_23ee8bcbL, 0xaecc4991_4078536dL, 0x823c1279_5db6ce57L,
			0xc2109436_4dfb5637L, 0x9096ea6f_3848984fL, 0xd77485cb_25823ac7L, 0xa086cfcd_97bf97f4L,
			0xef340a98_172aace5L, 0xb23867fb_2a35b28eL, 0x84c8d4df_d2c63f3bL, 0xc5dd4427_1ad3cdbaL,
			0x936b9fce_bb25c996L, 0xdbac6c24_7d62a584L, 0xa3ab6658_0d5fdaf6L, 0xf3e2f893_dec3f126L,
			0xb5b5ada8_aaff80b8L, 0x87625f05_6c7c4a8bL, 0xc9bcff60_34c13053L, 0x964e858c_91ba2655L,
			0xdff97724_70297ebdL, 0xa6dfbd9f_b8e5b88fL, 0xf8a95fcf_88747d94L, 0xb9447093_8fa89bcfL,
			0x8a08f0f8_bf0f156bL, 0xcdb02555_653131b6L, 0x993fe2c6_d07b7facL, 0xe45c10c4_2a2b3b06L,
			0xaa242499_697392d3L, 0xfd87b5f2_8300ca0eL, 0xbce50864_92111aebL, 0x8cbccc09_6f5088ccL,
			0xd1b71758_e219652cL, 0x9c400000_00000000L, 0xe8d4a510_00000000L, 0xad78ebc5_ac620000L,
			0x813f3978_f8940984L, 0xc097ce7b_c90715b3L, 0x8f7e32ce_7bea5c70L, 0xd5d238a4_abe98068L,
			0x9f4f2726_179a2245L, 0xed63a231_d4c4fb27L, 0xb0de6538_8cc8ada8L, 0x83c7088e_1aab65dbL,
			0xc45d1df9_42711d9aL, 0x924d692c_a61be758L, 0xda01ee64_1a708deaL, 0xa26da399_9aef774aL,
			0xf209787b_b47d6b85L, 0xb454e4a1_79dd1877L, 0x865b8692_5b9bc5c2L, 0xc83553c5_c8965d3dL,
			0x952ab45c_fa97a0b3L, 0xde469fbd_99a05fe3L, 0xa59bc234_db398c25L, 0xf6c69a72_a3989f5cL,
			0xb7dcbf53_54e9beceL, 0x88fcf317_f22241e2L, 0xcc20ce9b_d35c78a5L, 0x98165af3_7b2153dfL,
			0xe2a0b5dc_971f303aL, 0xa8d9d153_5ce3b396L, 0xfb9b7cd9_a4a7443cL, 0xbb764c4c_a7a44410L,
			0x8bab8eef_b6409c1aL, 0xd01fef10_a657842cL, 0x9b10a4e5_e9913129L, 0xe7109bfb_a19c0c9dL,
			0xac2820d9_623bf429L, 0x80444b5e_7aa7cf85L, 0xbf21e440_03acdd2dL, 0x8e679c2f_5e44ff8fL,
			0xd433179d_9c8cb841L, 0x9e19db92_b4e31ba9L, 0xeb96bf6e_badf77d9L, 0xaf87023b_9bf0ee6bL };

	private static final int[] CACHED_POWERS_E = { -1220, -1193, -1166, -1140, -1113, -1087, -1060, -1034, -1007, -980,
			-954, -927, -901, -874, -847, -821, -794, -768, -741, -715, -688, -661, -635, -608, -582, -555, -529, -502,
			-475, -449, -422, -396, -369, -343, -316, -289, -263, -236, -210, -183, -157, -130, -103, -77, -50, -24, 3,
			30, 56, 83, 109, 136, 162, 189, 216, 242, 269, 295, 322, 348, 375, 402, 428, 455, 481, 508, 534, 561, 588,
			614, 641, 667, 694, 720, 747, 774, 800, 827, 853, 880, 907, 933, 960, 986, 1013, 1039, 1066 };

	byte[] buf = new byte[32];
	int pos;

	static long umulHigh1(long a, long b) { // b < 0
		b &= 0x7FFF_FFFF_FFFF_FFFFL;
		long ab0, ab1;
		if (a >= 0) {
			ab0 = (((a * b) >>> 63) + (a & 1) + 1) >> 1;
			ab1 = Math.multiplyHigh(a, b) + (a >> 1);
		} else {
			a &= 0x7FFF_FFFF_FFFF_FFFFL;
			ab0 = (((a * b) >>> 63) + (a & 1) + (b & 1) + 1) >> 1;
			ab1 = Math.multiplyHigh(a, b) + (a >> 1) + (b >> 1) + (1L << 62);
		}
		return ab0 + ab1;
	}

	static long umulHigh2(long a, long b) { // a < 0 && b < 0
		a &= 0x7FFF_FFFF_FFFF_FFFFL;
		b &= 0x7FFF_FFFF_FFFF_FFFFL;
		long ab0 = (((a * b) >>> 63) + (a & 1) + (b & 1) + 1) >> 1;
		long ab1 = Math.multiplyHigh(a, b) + (a >> 1) + (b >> 1) + (1L << 62);
		return ab0 + ab1;
	}

	void grisuRound(final int len, final long delta, long rest, final long tenKappa, final long mpf) {
		while (Long.compareUnsigned(rest, mpf) < 0 && Long.compareUnsigned(delta - rest, tenKappa) >= 0
				&& (Long.compareUnsigned(rest + tenKappa, mpf) < 0 || // closer
						Long.compareUnsigned(mpf - rest, rest + tenKappa - mpf) > 0)) {
			buf[pos + len - 1]--;
			rest += tenKappa;
		}
	}

	void grisu2(final double d, final int maxDecimalPlaces) {
		// {f,e}.reset(d)
		long f = Double.doubleToRawLongBits(d);
		int e = (int) (f >>> DOUBLE_SIGNIFICAND_SIZE); // [0,0x3ff]
		f &= DOUBLE_SIGNIFICAND_MASK; // [0,1e52)
		if (e != 0) {
			f += DOUBLE_HIDDEN_BIT; // [1e52,1e53)
			e -= DOUBLE_EXP_BIAS; // [-0x432,-0x34]
		} else
			e = 1 - DOUBLE_EXP_BIAS; // -0x432 (1+DOUBLE_MIN_EXP)
		// {f,e}.normalizedBoundaries(mf/me, pf/pe);
		int pe = e - 1; // [-0x433,-0x35]
		long pf = (f << 1) + 1; // [1,1e54)
		//{ pf/e.normalizeBoundary(); // pf <<= Long.numberOfLeadingZeros(pf << DOUBLE_EXP_SIZE) + DOUBLE_EXP_SIZE;
		while ((pf & (DOUBLE_HIDDEN_BIT << 1)) == 0) { // max loop count: 53
			pe--;
			pf <<= 1;
		}
		pe -= DOUBLE_EXP_SIZE; // [-0x432-0x35-0xa=-0x3f3,-0x35-0xa=-0x3f]
		pf <<= DOUBLE_EXP_SIZE; // highest bit == 1
		//}
		long mf;
		final int me;
		if (f == DOUBLE_HIDDEN_BIT) {
			mf = (f << 2) - 1;
			me = e - 2;
		} else {
			mf = (f << 1) - 1;
			me = e - 1;
		}
		mf <<= me - pe;
		//}
		f <<= Long.numberOfLeadingZeros(f); // f.normalize(), highest bit == 1

		// getCachedPower(pe)
		// int k = static_cast<int>(ceil((-61 - e) * 0.30102999566398114)) + 374;
		final double dk = (-61 - pe) * 0.30102999566398114 + 347; // dk must be positive, so can do ceiling in positive
		int kk = (int) dk;
		if (dk - kk > 0)
			kk++;
		final int idx = (kk >> 3) + 1;
		kk = 348 - (idx << 3); // decimal exponent no need lookup table

		final long cmkf = CACHED_POWERS_F[idx]; // highest bit == 1
		f = umulHigh2(f, cmkf);
		pf = umulHigh2(pf, cmkf);
		mf = umulHigh1(mf, cmkf);
		e = -(pe + CACHED_POWERS_E[idx] + 64);
		long delta = pf-- - mf - 2;

		// digitGen(f, pf, e, delta)
		final long ff = 1L << e;
		int p1 = (int) (pf >>> e) & 0x7fff_ffff;
		long p2 = pf & (ff - 1), tmp;
		pf -= f;
		int len = 0, kappa, v; // kappa in [0, 9]
		// simple pure C++ implementation was faster than __builtin_clz version in this situation. @formatter:off
			 if (p1 <          10) kappa = 1;
		else if (p1 <         100) kappa = 2;
		else if (p1 <        1000) kappa = 3;
		else if (p1 <      1_0000) kappa = 4;
		else if (p1 <     10_0000) kappa = 5;
		else if (p1 <    100_0000) kappa = 6;
		else if (p1 <   1000_0000) kappa = 7;
		else if (p1 < 1_0000_0000) kappa = 8;
		else kappa = 9; // will not reach 10 digits: if (p1 < 10_0000_0000) kappa = 9; kappa = 10; @formatter:on
		do {
			switch (kappa) { //@formatter:off
				case 9:  v = p1 / 1_0000_0000; p1 %= 1_0000_0000; break;
				case 8:  v = p1 /   1000_0000; p1 %=   1000_0000; break;
				case 7:  v = p1 /    100_0000; p1 %=    100_0000; break;
				case 6:  v = p1 /     10_0000; p1 %=     10_0000; break;
				case 5:  v = p1 /      1_0000; p1 %=      1_0000; break;
				case 4:  v = p1 /        1000; p1 %=        1000; break;
				case 3:  v = p1 /         100; p1 %=         100; break;
				case 2:  v = p1 /          10; p1 %=          10; break;
				case 1:  v = p1;               p1  =           0; break;
				default: continue;
			} //@formatter:on
			if ((v | len) != 0)
				buf[pos + len++] = (byte) ('0' + v);
		} while (Long.compareUnsigned((tmp = (p1 << e) + p2), delta) > 0 && --kappa > 0);
		if (kappa != 0) {
			kk += --kappa;
			grisuRound(len, delta, tmp, (long) POW10[kappa] << e, pf);
		} else { // kappa == 0
			for (;;) {
				p2 *= 10;
				delta *= 10;
				final int b = (int) (p2 >>> e);
				if ((b | len) != 0)
					buf[pos + len++] = (byte) ('0' + b);
				p2 &= ff - 1;
				kappa++;
				if (Long.compareUnsigned(p2, delta) < 0) {
					kk -= kappa;
					grisuRound(len, delta, p2, ff, kappa < 9 ? POW10[kappa] * pf : 0);
					break;
				}
			}
		}

		// prettify(len, maxDecimalPlaces)
		int k = len + kk; // 10^(k-1) <= v < 10^k
		if (k <= 21) {
			if (kk >= 0) { // 1234e7 -> 12340000000
				for (int i = len; i < k; i++)
					buf[pos + i] = '0';
				buf[pos + k] = '.';
				buf[pos + k + 1] = '0';
				pos += k + 2;
				return;
			}
			if (k > 0) { // 1234e-2 -> 12.34
				System.arraycopy(buf, pos + k, buf, pos + k + 1, len - k);
				buf[pos + k] = '.';
				if (kk + maxDecimalPlaces < 0) {
					// when maxDecimalPlaces = 2, 1.2345 -> 1.23, 1.102 -> 1.1
					// remove extra trailing zeros (at least one) after truncation.
					for (int i = k + maxDecimalPlaces; i > k + 1; i--)
						if (buf[pos + i] != '0') {
							pos += i + 1;
							return;
						}
					pos += k + 2; // reserve one zero
					return;
				}
				pos += len + 1;
				return;
			}
		}
		if (-6 < k && k <= 0) { // 1234e-6 -> 0.001234
			final int offset = 2 - k;
			System.arraycopy(buf, pos, buf, pos + offset, len);
			buf[pos] = '0';
			buf[pos + 1] = '.';
			for (int i = 2; i < offset; i++)
				buf[pos + i] = '0';
			if (len - k > maxDecimalPlaces) {
				// when maxDecimalPlaces = 2, 0.123 -> 0.12, 0.102 -> 0.1
				// remove extra trailing zeros (at least one) after truncation
				for (int i = maxDecimalPlaces + 1; i > 2; i--)
					if (buf[pos + i] != '0') {
						pos += i + 1;
						return;
					}
				pos += 3; // reserve one zero
				return;
			}
			pos += len + offset;
			return;
		}
		if (k < -maxDecimalPlaces) { // truncate to zero
			buf[pos++] = '0';
			buf[pos++] = '.';
			buf[pos++] = '0';
			return;
		}
		if (len == 1) { // 1e30
			buf[pos + 1] = 'e';
			pos += 2;
		} else { // 1234e30 -> 1.234e33
			System.arraycopy(buf, pos + 1, buf, pos + 2, len - 1);
			buf[pos + 1] = '.';
			buf[pos + len + 1] = 'e';
			pos += len + 2;
		}
		// writeExponent(--k)
		if (--k < 0) {
			buf[pos++] = '-';
			k = -k;
		}
		if (k < 10) {
			buf[pos++] = (byte) ('0' + k);
			return;
		}
		if (k >= 100) {
			buf[pos++] = (byte) ('0' + k / 100);
			k %= 100;
		}
		buf[pos++] = (byte) ('0' + k / 10);
		buf[pos++] = (byte) ('0' + k % 10);
	}

	public void writeDouble(double d, final int maxDecimalPlaces) {
		final long u = Double.doubleToRawLongBits(d);
		if ((u & (DOUBLE_EXP_MASK | DOUBLE_SIGNIFICAND_MASK)) == 0) { // d == 0
			if ((u & DOUBLE_SIGN_MASK) != 0)
				buf[pos++] = '-';
			buf[pos++] = '0';
			buf[pos++] = '.';
			buf[pos++] = '0';
			return;
		}
		if (d < 0) {
			buf[pos++] = '-';
			d = -d;
		}
		grisu2(d, maxDecimalPlaces);
	}

	public void writeDouble(final double d) {
		writeDouble(d, 324);
	}

	public void writeFloat(final float f, final int maxDecimalPlaces) {
		writeDouble(f, maxDecimalPlaces);
	}

	public void writeFloat(final float f) {
		writeDouble(f, 324);
	}

	public void writeInt(int value) {
		if (value < 0) {
			if (value == Integer.MIN_VALUE) {
				writeLong(value);
				return;
			}
			buf[pos++] = '-';
			value = -value;
		}
		if (value < 1_0000) {
			final int d1 = (value / 100) << 1;
			final int d2 = (value % 100) << 1;
			if (value >= 1000)
				buf[pos++] = DIGITES_LUT[d1];
			if (value >= 100)
				buf[pos++] = DIGITES_LUT[d1 + 1];
			if (value >= 10)
				buf[pos++] = DIGITES_LUT[d2];
			buf[pos++] = DIGITES_LUT[d2 + 1];
		} else if (value < 1_0000_0000) { // value = bbbb_cccc
			final int b = value / 1_0000;
			final int c = value % 1_0000;
			final int d1 = (b / 100) << 1;
			final int d2 = (b % 100) << 1;
			final int d3 = (c / 100) << 1;
			final int d4 = (c % 100) << 1;
			if (value >= 1000_0000)
				buf[pos++] = DIGITES_LUT[d1];
			if (value >= 100_0000)
				buf[pos++] = DIGITES_LUT[d1 + 1];
			if (value >= 10_0000)
				buf[pos++] = DIGITES_LUT[d2];
			buf[pos++] = DIGITES_LUT[d2 + 1];
			buf[pos++] = DIGITES_LUT[d3];
			buf[pos++] = DIGITES_LUT[d3 + 1];
			buf[pos++] = DIGITES_LUT[d4];
			buf[pos++] = DIGITES_LUT[d4 + 1];
		} else { // value = aa_bbbb_cccc in decimal
			final int a = value / 1_0000_0000; // [1,21]
			value %= 1_0000_0000;
			if (a >= 10) {
				final int i = a << 1;
				buf[pos++] = DIGITES_LUT[i];
				buf[pos++] = DIGITES_LUT[i + 1];
			} else
				buf[pos++] = (byte) ('0' + a);
			final int b = value / 1_0000;
			final int c = value % 1_0000;
			final int d1 = (b / 100) << 1;
			final int d2 = (b % 100) << 1;
			final int d3 = (c / 100) << 1;
			final int d4 = (c % 100) << 1;
			buf[pos++] = DIGITES_LUT[d1];
			buf[pos++] = DIGITES_LUT[d1 + 1];
			buf[pos++] = DIGITES_LUT[d2];
			buf[pos++] = DIGITES_LUT[d2 + 1];
			buf[pos++] = DIGITES_LUT[d3];
			buf[pos++] = DIGITES_LUT[d3 + 1];
			buf[pos++] = DIGITES_LUT[d4];
			buf[pos++] = DIGITES_LUT[d4 + 1];
		}
	}

	public void writeLong(long value) { // 7FFF_FFFF_FFFF_FFFF = 922_3372_0368_5477_5807
		if (value < 0) {
			if (value == Long.MIN_VALUE) {
				System.arraycopy("-9223372036854775808".getBytes(StandardCharsets.ISO_8859_1), 0, buf, pos, 20);
				pos += 20;
				return;
			}
			buf[pos++] = '-';
			value = -value;
		}
		if (value < 1_0000_0000) {
			int v = (int) value;
			if (v < 1_0000) {
				final int d1 = (v / 100) << 1;
				final int d2 = (v % 100) << 1;
				if (v >= 1000)
					buf[pos++] = DIGITES_LUT[d1];
				if (v >= 100)
					buf[pos++] = DIGITES_LUT[d1 + 1];
				if (v >= 10)
					buf[pos++] = DIGITES_LUT[d2];
				buf[pos++] = DIGITES_LUT[d2 + 1];
			} else { // value = bbbbcccc
				final int b = v / 1_0000;
				final int c = v % 1_0000;
				final int d1 = (b / 100) << 1;
				final int d2 = (b % 100) << 1;
				final int d3 = (c / 100) << 1;
				final int d4 = (c % 100) << 1;
				if (value >= 1000_0000)
					buf[pos++] = DIGITES_LUT[d1];
				if (value >= 100_0000)
					buf[pos++] = DIGITES_LUT[d1 + 1];
				if (value >= 10_0000)
					buf[pos++] = DIGITES_LUT[d2];
				buf[pos++] = DIGITES_LUT[d2 + 1];
				buf[pos++] = DIGITES_LUT[d3];
				buf[pos++] = DIGITES_LUT[d3 + 1];
				buf[pos++] = DIGITES_LUT[d4];
				buf[pos++] = DIGITES_LUT[d4 + 1];
			}
		} else if (value < 1_0000_0000_0000_0000L) {
			final int v0 = (int) (value / 1_0000_0000);
			final int v1 = (int) (value % 1_0000_0000);
			final int b0 = v0 / 1_0000;
			final int c0 = v0 % 1_0000;
			final int d1 = (b0 / 100) << 1;
			final int d2 = (b0 % 100) << 1;
			final int d3 = (c0 / 100) << 1;
			final int d4 = (c0 % 100) << 1;
			final int b1 = v1 / 1_0000;
			final int c1 = v1 % 1_0000;
			final int d5 = (b1 / 100) << 1;
			final int d6 = (b1 % 100) << 1;
			final int d7 = (c1 / 100) << 1;
			final int d8 = (c1 % 100) << 1;
			if (value >= 1000_0000_0000_0000L)
				buf[pos++] = DIGITES_LUT[d1];
			if (value >= 100_0000_0000_0000L)
				buf[pos++] = DIGITES_LUT[d1 + 1];
			if (value >= 10_0000_0000_0000L)
				buf[pos++] = DIGITES_LUT[d2];
			if (value >= 1_0000_0000_0000L)
				buf[pos++] = DIGITES_LUT[d2 + 1];
			if (value >= 1000_0000_0000L)
				buf[pos++] = DIGITES_LUT[d3];
			if (value >= 100_0000_0000L)
				buf[pos++] = DIGITES_LUT[d3 + 1];
			if (value >= 10_0000_0000L)
				buf[pos++] = DIGITES_LUT[d4];
			buf[pos++] = DIGITES_LUT[d4 + 1];
			buf[pos++] = DIGITES_LUT[d5];
			buf[pos++] = DIGITES_LUT[d5 + 1];
			buf[pos++] = DIGITES_LUT[d6];
			buf[pos++] = DIGITES_LUT[d6 + 1];
			buf[pos++] = DIGITES_LUT[d7];
			buf[pos++] = DIGITES_LUT[d7 + 1];
			buf[pos++] = DIGITES_LUT[d8];
			buf[pos++] = DIGITES_LUT[d8 + 1];
		} else {
			final int a = (int) (value / 1_0000_0000_0000_0000L); // [1,922]
			value %= 1_0000_0000_0000_0000L;
			if (a < 10)
				buf[pos++] = (byte) ('0' + a);
			else if (a < 100) {
				final int i = a << 1;
				buf[pos++] = DIGITES_LUT[i];
				buf[pos++] = DIGITES_LUT[i + 1];
			} else if (a < 1000) {
				buf[pos++] = (byte) ('0' + a / 100);
				final int i = (a % 100) << 1;
				buf[pos++] = DIGITES_LUT[i];
				buf[pos++] = DIGITES_LUT[i + 1];
			} else {
				final int i = (a / 100) << 1;
				final int j = (a % 100) << 1;
				buf[pos++] = DIGITES_LUT[i];
				buf[pos++] = DIGITES_LUT[i + 1];
				buf[pos++] = DIGITES_LUT[j];
				buf[pos++] = DIGITES_LUT[j + 1];
			}
			final int v0 = (int) (value / 1_0000_0000);
			final int v1 = (int) (value % 1_0000_0000);
			final int b0 = v0 / 1_0000;
			final int c0 = v0 % 1_0000;
			final int d1 = (b0 / 100) << 1;
			final int d2 = (b0 % 100) << 1;
			final int d3 = (c0 / 100) << 1;
			final int d4 = (c0 % 100) << 1;
			final int b1 = v1 / 1_0000;
			final int c1 = v1 % 1_0000;
			final int d5 = (b1 / 100) << 1;
			final int d6 = (b1 % 100) << 1;
			final int d7 = (c1 / 100) << 1;
			final int d8 = (c1 % 100) << 1;
			buf[pos++] = DIGITES_LUT[d1];
			buf[pos++] = DIGITES_LUT[d1 + 1];
			buf[pos++] = DIGITES_LUT[d2];
			buf[pos++] = DIGITES_LUT[d2 + 1];
			buf[pos++] = DIGITES_LUT[d3];
			buf[pos++] = DIGITES_LUT[d3 + 1];
			buf[pos++] = DIGITES_LUT[d4];
			buf[pos++] = DIGITES_LUT[d4 + 1];
			buf[pos++] = DIGITES_LUT[d5];
			buf[pos++] = DIGITES_LUT[d5 + 1];
			buf[pos++] = DIGITES_LUT[d6];
			buf[pos++] = DIGITES_LUT[d6 + 1];
			buf[pos++] = DIGITES_LUT[d7];
			buf[pos++] = DIGITES_LUT[d7 + 1];
			buf[pos++] = DIGITES_LUT[d8];
			buf[pos++] = DIGITES_LUT[d8 + 1];
		}
	}

	public static void main(String[] args) {
		double[] tests = { 3.1415926, 31415926, 0.31415926, 314.15926, 3.1415926e7, 3.1415926E-7, 0, 1.0 };
		JasonWriter jw = new JasonWriter();
		int n = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
//				n += Double.toString(tests[j]).length();
				jw.pos = 0;
				jw.writeDouble(tests[j]);
				n += jw.pos;
//				System.out.println(new String(jw.buf, 0, jw.pos));
			}
		}
		System.out.format("%d", n); // 660000000
	}
}
