package jason;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import sun.misc.Unsafe;

//ref: https://github.com/jk-jeon/dragonbox/tree/master/subproject/simple
@SuppressWarnings("SameParameterValue")
public class DragonBox {
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
			'4', '9', '5', '9', '6', '9', '7', '9', '8', '9', '9'};

	private static final long[] FLOAT_TABLE = { // uint64[78]
			0x81ceb32c4b43fcf5L, 0xa2425ff75e14fc32L,
			0xcad2f7f5359a3b3fL, 0xfd87b5f28300ca0eL,
			0x9e74d1b791e07e49L, 0xc612062576589ddbL,
			0xf79687aed3eec552L, 0x9abe14cd44753b53L,
			0xc16d9a0095928a28L, 0xf1c90080baf72cb2L,
			0x971da05074da7befL, 0xbce5086492111aebL,
			0xec1e4a7db69561a6L, 0x9392ee8e921d5d08L,
			0xb877aa3236a4b44aL, 0xe69594bec44de15cL,
			0x901d7cf73ab0acdaL, 0xb424dc35095cd810L,
			0xe12e13424bb40e14L, 0x8cbccc096f5088ccL,
			0xafebff0bcb24aaffL, 0xdbe6fecebdedd5bfL,
			0x89705f4136b4a598L, 0xabcc77118461cefdL,
			0xd6bf94d5e57a42bdL, 0x8637bd05af6c69b6L,
			0xa7c5ac471b478424L, 0xd1b71758e219652cL,
			0x83126e978d4fdf3cL, 0xa3d70a3d70a3d70bL,
			0xcccccccccccccccdL, 0x8000000000000000L,
			0xa000000000000000L, 0xc800000000000000L,
			0xfa00000000000000L, 0x9c40000000000000L,
			0xc350000000000000L, 0xf424000000000000L,
			0x9896800000000000L, 0xbebc200000000000L,
			0xee6b280000000000L, 0x9502f90000000000L,
			0xba43b74000000000L, 0xe8d4a51000000000L,
			0x9184e72a00000000L, 0xb5e620f480000000L,
			0xe35fa931a0000000L, 0x8e1bc9bf04000000L,
			0xb1a2bc2ec5000000L, 0xde0b6b3a76400000L,
			0x8ac7230489e80000L, 0xad78ebc5ac620000L,
			0xd8d726b7177a8000L, 0x878678326eac9000L,
			0xa968163f0a57b400L, 0xd3c21bcecceda100L,
			0x84595161401484a0L, 0xa56fa5b99019a5c8L,
			0xcecb8f27f4200f3aL, 0x813f3978f8940985L,
			0xa18f07d736b90be6L, 0xc9f2c9cd04674edfL,
			0xfc6f7c4045812297L, 0x9dc5ada82b70b59eL,
			0xc5371912364ce306L, 0xf684df56c3e01bc7L,
			0x9a130b963a6c115dL, 0xc097ce7bc90715b4L,
			0xf0bdc21abb48db21L, 0x96769950b50d88f5L,
			0xbc143fa4e250eb32L, 0xeb194f8e1ae525feL,
			0x92efd1b8d0cf37bfL, 0xb7abc627050305aeL,
			0xe596b7b0c643c71aL, 0x8f7e32ce7bea5c70L,
			0xb35dbf821ae4f38cL, 0xe0352f62a19e306fL,
	};

	private static final long[] DOUBLE_TABLE = { // uint128(high,low)[619]
			0xff77b1fcbebcdc4fL, 0x25e8e89c13bb0f7bL,
			0x9faacf3df73609b1L, 0x77b191618c54e9adL,
			0xc795830d75038c1dL, 0xd59df5b9ef6a2418L,
			0xf97ae3d0d2446f25L, 0x4b0573286b44ad1eL,
			0x9becce62836ac577L, 0x4ee367f9430aec33L,
			0xc2e801fb244576d5L, 0x229c41f793cda740L,
			0xf3a20279ed56d48aL, 0x6b43527578c11110L,
			0x9845418c345644d6L, 0x830a13896b78aaaaL,
			0xbe5691ef416bd60cL, 0x23cc986bc656d554L,
			0xedec366b11c6cb8fL, 0x2cbfbe86b7ec8aa9L,
			0x94b3a202eb1c3f39L, 0x7bf7d71432f3d6aaL,
			0xb9e08a83a5e34f07L, 0xdaf5ccd93fb0cc54L,
			0xe858ad248f5c22c9L, 0xd1b3400f8f9cff69L,
			0x91376c36d99995beL, 0x23100809b9c21fa2L,
			0xb58547448ffffb2dL, 0xabd40a0c2832a78bL,
			0xe2e69915b3fff9f9L, 0x16c90c8f323f516dL,
			0x8dd01fad907ffc3bL, 0xae3da7d97f6792e4L,
			0xb1442798f49ffb4aL, 0x99cd11cfdf41779dL,
			0xdd95317f31c7fa1dL, 0x40405643d711d584L,
			0x8a7d3eef7f1cfc52L, 0x482835ea666b2573L,
			0xad1c8eab5ee43b66L, 0xda3243650005eed0L,
			0xd863b256369d4a40L, 0x90bed43e40076a83L,
			0x873e4f75e2224e68L, 0x5a7744a6e804a292L,
			0xa90de3535aaae202L, 0x711515d0a205cb37L,
			0xd3515c2831559a83L, 0x0d5a5b44ca873e04L,
			0x8412d9991ed58091L, 0xe858790afe9486c3L,
			0xa5178fff668ae0b6L, 0x626e974dbe39a873L,
			0xce5d73ff402d98e3L, 0xfb0a3d212dc81290L,
			0x80fa687f881c7f8eL, 0x7ce66634bc9d0b9aL,
			0xa139029f6a239f72L, 0x1c1fffc1ebc44e81L,
			0xc987434744ac874eL, 0xa327ffb266b56221L,
			0xfbe9141915d7a922L, 0x4bf1ff9f0062baa9L,
			0x9d71ac8fada6c9b5L, 0x6f773fc3603db4aaL,
			0xc4ce17b399107c22L, 0xcb550fb4384d21d4L,
			0xf6019da07f549b2bL, 0x7e2a53a146606a49L,
			0x99c102844f94e0fbL, 0x2eda7444cbfc426eL,
			0xc0314325637a1939L, 0xfa911155fefb5309L,
			0xf03d93eebc589f88L, 0x793555ab7eba27cbL,
			0x96267c7535b763b5L, 0x4bc1558b2f3458dfL,
			0xbbb01b9283253ca2L, 0x9eb1aaedfb016f17L,
			0xea9c227723ee8bcbL, 0x465e15a979c1caddL,
			0x92a1958a7675175fL, 0x0bfacd89ec191ecaL,
			0xb749faed14125d36L, 0xcef980ec671f667cL,
			0xe51c79a85916f484L, 0x82b7e12780e7401bL,
			0x8f31cc0937ae58d2L, 0xd1b2ecb8b0908811L,
			0xb2fe3f0b8599ef07L, 0x861fa7e6dcb4aa16L,
			0xdfbdcece67006ac9L, 0x67a791e093e1d49bL,
			0x8bd6a141006042bdL, 0xe0c8bb2c5c6d24e1L,
			0xaecc49914078536dL, 0x58fae9f773886e19L,
			0xda7f5bf590966848L, 0xaf39a475506a899fL,
			0x888f99797a5e012dL, 0x6d8406c952429604L,
			0xaab37fd7d8f58178L, 0xc8e5087ba6d33b84L,
			0xd5605fcdcf32e1d6L, 0xfb1e4a9a90880a65L,
			0x855c3be0a17fcd26L, 0x5cf2eea09a550680L,
			0xa6b34ad8c9dfc06fL, 0xf42faa48c0ea481fL,
			0xd0601d8efc57b08bL, 0xf13b94daf124da27L,
			0x823c12795db6ce57L, 0x76c53d08d6b70859L,
			0xa2cb1717b52481edL, 0x54768c4b0c64ca6fL,
			0xcb7ddcdda26da268L, 0xa9942f5dcf7dfd0aL,
			0xfe5d54150b090b02L, 0xd3f93b35435d7c4dL,
			0x9efa548d26e5a6e1L, 0xc47bc5014a1a6db0L,
			0xc6b8e9b0709f109aL, 0x359ab6419ca1091cL,
			0xf867241c8cc6d4c0L, 0xc30163d203c94b63L,
			0x9b407691d7fc44f8L, 0x79e0de63425dcf1eL,
			0xc21094364dfb5636L, 0x985915fc12f542e5L,
			0xf294b943e17a2bc4L, 0x3e6f5b7b17b2939eL,
			0x979cf3ca6cec5b5aL, 0xa705992ceecf9c43L,
			0xbd8430bd08277231L, 0x50c6ff782a838354L,
			0xece53cec4a314ebdL, 0xa4f8bf5635246429L,
			0x940f4613ae5ed136L, 0x871b7795e136be9aL,
			0xb913179899f68584L, 0x28e2557b59846e40L,
			0xe757dd7ec07426e5L, 0x331aeada2fe589d0L,
			0x9096ea6f3848984fL, 0x3ff0d2c85def7622L,
			0xb4bca50b065abe63L, 0x0fed077a756b53aaL,
			0xe1ebce4dc7f16dfbL, 0xd3e8495912c62895L,
			0x8d3360f09cf6e4bdL, 0x64712dd7abbbd95dL,
			0xb080392cc4349decL, 0xbd8d794d96aacfb4L,
			0xdca04777f541c567L, 0xecf0d7a0fc5583a1L,
			0x89e42caaf9491b60L, 0xf41686c49db57245L,
			0xac5d37d5b79b6239L, 0x311c2875c522ced6L,
			0xd77485cb25823ac7L, 0x7d633293366b828cL,
			0x86a8d39ef77164bcL, 0xae5dff9c02033198L,
			0xa8530886b54dbdebL, 0xd9f57f830283fdfdL,
			0xd267caa862a12d66L, 0xd072df63c324fd7cL,
			0x8380dea93da4bc60L, 0x4247cb9e59f71e6eL,
			0xa46116538d0deb78L, 0x52d9be85f074e609L,
			0xcd795be870516656L, 0x67902e276c921f8cL,
			0x806bd9714632dff6L, 0x00ba1cd8a3db53b7L,
			0xa086cfcd97bf97f3L, 0x80e8a40eccd228a5L,
			0xc8a883c0fdaf7df0L, 0x6122cd128006b2ceL,
			0xfad2a4b13d1b5d6cL, 0x796b805720085f82L,
			0x9cc3a6eec6311a63L, 0xcbe3303674053bb1L,
			0xc3f490aa77bd60fcL, 0xbedbfc4411068a9dL,
			0xf4f1b4d515acb93bL, 0xee92fb5515482d45L,
			0x991711052d8bf3c5L, 0x751bdd152d4d1c4bL,
			0xbf5cd54678eef0b6L, 0xd262d45a78a0635eL,
			0xef340a98172aace4L, 0x86fb897116c87c35L,
			0x9580869f0e7aac0eL, 0xd45d35e6ae3d4da1L,
			0xbae0a846d2195712L, 0x8974836059cca10aL,
			0xe998d258869facd7L, 0x2bd1a438703fc94cL,
			0x91ff83775423cc06L, 0x7b6306a34627ddd0L,
			0xb67f6455292cbf08L, 0x1a3bc84c17b1d543L,
			0xe41f3d6a7377eecaL, 0x20caba5f1d9e4a94L,
			0x8e938662882af53eL, 0x547eb47b7282ee9dL,
			0xb23867fb2a35b28dL, 0xe99e619a4f23aa44L,
			0xdec681f9f4c31f31L, 0x6405fa00e2ec94d5L,
			0x8b3c113c38f9f37eL, 0xde83bc408dd3dd05L,
			0xae0b158b4738705eL, 0x9624ab50b148d446L,
			0xd98ddaee19068c76L, 0x3badd624dd9b0958L,
			0x87f8a8d4cfa417c9L, 0xe54ca5d70a80e5d7L,
			0xa9f6d30a038d1dbcL, 0x5e9fcf4ccd211f4dL,
			0xd47487cc8470652bL, 0x7647c32000696720L,
			0x84c8d4dfd2c63f3bL, 0x29ecd9f40041e074L,
			0xa5fb0a17c777cf09L, 0xf468107100525891L,
			0xcf79cc9db955c2ccL, 0x7182148d4066eeb5L,
			0x81ac1fe293d599bfL, 0xc6f14cd848405531L,
			0xa21727db38cb002fL, 0xb8ada00e5a506a7dL,
			0xca9cf1d206fdc03bL, 0xa6d90811f0e4851dL,
			0xfd442e4688bd304aL, 0x908f4a166d1da664L,
			0x9e4a9cec15763e2eL, 0x9a598e4e043287ffL,
			0xc5dd44271ad3cdbaL, 0x40eff1e1853f29feL,
			0xf7549530e188c128L, 0xd12bee59e68ef47dL,
			0x9a94dd3e8cf578b9L, 0x82bb74f8301958cfL,
			0xc13a148e3032d6e7L, 0xe36a52363c1faf02L,
			0xf18899b1bc3f8ca1L, 0xdc44e6c3cb279ac2L,
			0x96f5600f15a7b7e5L, 0x29ab103a5ef8c0baL,
			0xbcb2b812db11a5deL, 0x7415d448f6b6f0e8L,
			0xebdf661791d60f56L, 0x111b495b3464ad22L,
			0x936b9fcebb25c995L, 0xcab10dd900beec35L,
			0xb84687c269ef3bfbL, 0x3d5d514f40eea743L,
			0xe65829b3046b0afaL, 0x0cb4a5a3112a5113L,
			0x8ff71a0fe2c2e6dcL, 0x47f0e785eaba72acL,
			0xb3f4e093db73a093L, 0x59ed216765690f57L,
			0xe0f218b8d25088b8L, 0x306869c13ec3532dL,
			0x8c974f7383725573L, 0x1e414218c73a13fcL,
			0xafbd2350644eeacfL, 0xe5d1929ef90898fbL,
			0xdbac6c247d62a583L, 0xdf45f746b74abf3aL,
			0x894bc396ce5da772L, 0x6b8bba8c328eb784L,
			0xab9eb47c81f5114fL, 0x066ea92f3f326565L,
			0xd686619ba27255a2L, 0xc80a537b0efefebeL,
			0x8613fd0145877585L, 0xbd06742ce95f5f37L,
			0xa798fc4196e952e7L, 0x2c48113823b73705L,
			0xd17f3b51fca3a7a0L, 0xf75a15862ca504c6L,
			0x82ef85133de648c4L, 0x9a984d73dbe722fcL,
			0xa3ab66580d5fdaf5L, 0xc13e60d0d2e0ebbbL,
			0xcc963fee10b7d1b3L, 0x318df905079926a9L,
			0xffbbcfe994e5c61fL, 0xfdf17746497f7053L,
			0x9fd561f1fd0f9bd3L, 0xfeb6ea8bedefa634L,
			0xc7caba6e7c5382c8L, 0xfe64a52ee96b8fc1L,
			0xf9bd690a1b68637bL, 0x3dfdce7aa3c673b1L,
			0x9c1661a651213e2dL, 0x06bea10ca65c084fL,
			0xc31bfa0fe5698db8L, 0x486e494fcff30a63L,
			0xf3e2f893dec3f126L, 0x5a89dba3c3efccfbL,
			0x986ddb5c6b3a76b7L, 0xf89629465a75e01dL,
			0xbe89523386091465L, 0xf6bbb397f1135824L,
			0xee2ba6c0678b597fL, 0x746aa07ded582e2dL,
			0x94db483840b717efL, 0xa8c2a44eb4571cddL,
			0xba121a4650e4ddebL, 0x92f34d62616ce414L,
			0xe896a0d7e51e1566L, 0x77b020baf9c81d18L,
			0x915e2486ef32cd60L, 0x0ace1474dc1d122fL,
			0xb5b5ada8aaff80b8L, 0x0d819992132456bbL,
			0xe3231912d5bf60e6L, 0x10e1fff697ed6c6aL,
			0x8df5efabc5979c8fL, 0xca8d3ffa1ef463c2L,
			0xb1736b96b6fd83b3L, 0xbd308ff8a6b17cb3L,
			0xddd0467c64bce4a0L, 0xac7cb3f6d05ddbdfL,
			0x8aa22c0dbef60ee4L, 0x6bcdf07a423aa96cL,
			0xad4ab7112eb3929dL, 0x86c16c98d2c953c7L,
			0xd89d64d57a607744L, 0xe871c7bf077ba8b8L,
			0x87625f056c7c4a8bL, 0x11471cd764ad4973L,
			0xa93af6c6c79b5d2dL, 0xd598e40d3dd89bd0L,
			0xd389b47879823479L, 0x4aff1d108d4ec2c4L,
			0x843610cb4bf160cbL, 0xcedf722a585139bbL,
			0xa54394fe1eedb8feL, 0xc2974eb4ee658829L,
			0xce947a3da6a9273eL, 0x733d226229feea33L,
			0x811ccc668829b887L, 0x0806357d5a3f5260L,
			0xa163ff802a3426a8L, 0xca07c2dcb0cf26f8L,
			0xc9bcff6034c13052L, 0xfc89b393dd02f0b6L,
			0xfc2c3f3841f17c67L, 0xbbac2078d443ace3L,
			0x9d9ba7832936edc0L, 0xd54b944b84aa4c0eL,
			0xc5029163f384a931L, 0x0a9e795e65d4df12L,
			0xf64335bcf065d37dL, 0x4d4617b5ff4a16d6L,
			0x99ea0196163fa42eL, 0x504bced1bf8e4e46L,
			0xc06481fb9bcf8d39L, 0xe45ec2862f71e1d7L,
			0xf07da27a82c37088L, 0x5d767327bb4e5a4dL,
			0x964e858c91ba2655L, 0x3a6a07f8d510f870L,
			0xbbe226efb628afeaL, 0x890489f70a55368cL,
			0xeadab0aba3b2dbe5L, 0x2b45ac74ccea842fL,
			0x92c8ae6b464fc96fL, 0x3b0b8bc90012929eL,
			0xb77ada0617e3bbcbL, 0x09ce6ebb40173745L,
			0xe55990879ddcaabdL, 0xcc420a6a101d0516L,
			0x8f57fa54c2a9eab6L, 0x9fa946824a12232eL,
			0xb32df8e9f3546564L, 0x47939822dc96abfaL,
			0xdff9772470297ebdL, 0x59787e2b93bc56f8L,
			0x8bfbea76c619ef36L, 0x57eb4edb3c55b65bL,
			0xaefae51477a06b03L, 0xede622920b6b23f2L,
			0xdab99e59958885c4L, 0xe95fab368e45eceeL,
			0x88b402f7fd75539bL, 0x11dbcb0218ebb415L,
			0xaae103b5fcd2a881L, 0xd652bdc29f26a11aL,
			0xd59944a37c0752a2L, 0x4be76d3346f04960L,
			0x857fcae62d8493a5L, 0x6f70a4400c562ddcL,
			0xa6dfbd9fb8e5b88eL, 0xcb4ccd500f6bb953L,
			0xd097ad07a71f26b2L, 0x7e2000a41346a7a8L,
			0x825ecc24c873782fL, 0x8ed400668c0c28c9L,
			0xa2f67f2dfa90563bL, 0x728900802f0f32fbL,
			0xcbb41ef979346bcaL, 0x4f2b40a03ad2ffbaL,
			0xfea126b7d78186bcL, 0xe2f610c84987bfa9L,
			0x9f24b832e6b0f436L, 0x0dd9ca7d2df4d7caL,
			0xc6ede63fa05d3143L, 0x91503d1c79720dbcL,
			0xf8a95fcf88747d94L, 0x75a44c6397ce912bL,
			0x9b69dbe1b548ce7cL, 0xc986afbe3ee11abbL,
			0xc24452da229b021bL, 0xfbe85badce996169L,
			0xf2d56790ab41c2a2L, 0xfae27299423fb9c4L,
			0x97c560ba6b0919a5L, 0xdccd879fc967d41bL,
			0xbdb6b8e905cb600fL, 0x5400e987bbc1c921L,
			0xed246723473e3813L, 0x290123e9aab23b69L,
			0x9436c0760c86e30bL, 0xf9a0b6720aaf6522L,
			0xb94470938fa89bceL, 0xf808e40e8d5b3e6aL,
			0xe7958cb87392c2c2L, 0xb60b1d1230b20e05L,
			0x90bd77f3483bb9b9L, 0xb1c6f22b5e6f48c3L,
			0xb4ecd5f01a4aa828L, 0x1e38aeb6360b1af4L,
			0xe2280b6c20dd5232L, 0x25c6da63c38de1b1L,
			0x8d590723948a535fL, 0x579c487e5a38ad0fL,
			0xb0af48ec79ace837L, 0x2d835a9df0c6d852L,
			0xdcdb1b2798182244L, 0xf8e431456cf88e66L,
			0x8a08f0f8bf0f156bL, 0x1b8e9ecb641b5900L,
			0xac8b2d36eed2dac5L, 0xe272467e3d222f40L,
			0xd7adf884aa879177L, 0x5b0ed81dcc6abb10L,
			0x86ccbb52ea94baeaL, 0x98e947129fc2b4eaL,
			0xa87fea27a539e9a5L, 0x3f2398d747b36225L,
			0xd29fe4b18e88640eL, 0x8eec7f0d19a03aaeL,
			0x83a3eeeef9153e89L, 0x1953cf68300424adL,
			0xa48ceaaab75a8e2bL, 0x5fa8c3423c052dd8L,
			0xcdb02555653131b6L, 0x3792f412cb06794eL,
			0x808e17555f3ebf11L, 0xe2bbd88bbee40bd1L,
			0xa0b19d2ab70e6ed6L, 0x5b6aceaeae9d0ec5L,
			0xc8de047564d20a8bL, 0xf245825a5a445276L,
			0xfb158592be068d2eL, 0xeed6e2f0f0d56713L,
			0x9ced737bb6c4183dL, 0x55464dd69685606cL,
			0xc428d05aa4751e4cL, 0xaa97e14c3c26b887L,
			0xf53304714d9265dfL, 0xd53dd99f4b3066a9L,
			0x993fe2c6d07b7fabL, 0xe546a8038efe402aL,
			0xbf8fdb78849a5f96L, 0xde98520472bdd034L,
			0xef73d256a5c0f77cL, 0x963e66858f6d4441L,
			0x95a8637627989aadL, 0xdde7001379a44aa9L,
			0xbb127c53b17ec159L, 0x5560c018580d5d53L,
			0xe9d71b689dde71afL, 0xaab8f01e6e10b4a7L,
			0x9226712162ab070dL, 0xcab3961304ca70e9L,
			0xb6b00d69bb55c8d1L, 0x3d607b97c5fd0d23L,
			0xe45c10c42a2b3b05L, 0x8cb89a7db77c506bL,
			0x8eb98a7a9a5b04e3L, 0x77f3608e92adb243L,
			0xb267ed1940f1c61cL, 0x55f038b237591ed4L,
			0xdf01e85f912e37a3L, 0x6b6c46dec52f6689L,
			0x8b61313bbabce2c6L, 0x2323ac4b3b3da016L,
			0xae397d8aa96c1b77L, 0xabec975e0a0d081bL,
			0xd9c7dced53c72255L, 0x96e7bd358c904a22L,
			0x881cea14545c7575L, 0x7e50d64177da2e55L,
			0xaa242499697392d2L, 0xdde50bd1d5d0b9eaL,
			0xd4ad2dbfc3d07787L, 0x955e4ec64b44e865L,
			0x84ec3c97da624ab4L, 0xbd5af13bef0b113fL,
			0xa6274bbdd0fadd61L, 0xecb1ad8aeacdd58fL,
			0xcfb11ead453994baL, 0x67de18eda5814af3L,
			0x81ceb32c4b43fcf4L, 0x80eacf948770ced8L,
			0xa2425ff75e14fc31L, 0xa1258379a94d028eL,
			0xcad2f7f5359a3b3eL, 0x096ee45813a04331L,
			0xfd87b5f28300ca0dL, 0x8bca9d6e188853fdL,
			0x9e74d1b791e07e48L, 0x775ea264cf55347eL,
			0xc612062576589ddaL, 0x95364afe032a819eL,
			0xf79687aed3eec551L, 0x3a83ddbd83f52205L,
			0x9abe14cd44753b52L, 0xc4926a9672793543L,
			0xc16d9a0095928a27L, 0x75b7053c0f178294L,
			0xf1c90080baf72cb1L, 0x5324c68b12dd6339L,
			0x971da05074da7beeL, 0xd3f6fc16ebca5e04L,
			0xbce5086492111aeaL, 0x88f4bb1ca6bcf585L,
			0xec1e4a7db69561a5L, 0x2b31e9e3d06c32e6L,
			0x9392ee8e921d5d07L, 0x3aff322e62439fd0L,
			0xb877aa3236a4b449L, 0x09befeb9fad487c3L,
			0xe69594bec44de15bL, 0x4c2ebe687989a9b4L,
			0x901d7cf73ab0acd9L, 0x0f9d37014bf60a11L,
			0xb424dc35095cd80fL, 0x538484c19ef38c95L,
			0xe12e13424bb40e13L, 0x2865a5f206b06fbaL,
			0x8cbccc096f5088cbL, 0xf93f87b7442e45d4L,
			0xafebff0bcb24aafeL, 0xf78f69a51539d749L,
			0xdbe6fecebdedd5beL, 0xb573440e5a884d1cL,
			0x89705f4136b4a597L, 0x31680a88f8953031L,
			0xabcc77118461cefcL, 0xfdc20d2b36ba7c3eL,
			0xd6bf94d5e57a42bcL, 0x3d32907604691b4dL,
			0x8637bd05af6c69b5L, 0xa63f9a49c2c1b110L,
			0xa7c5ac471b478423L, 0x0fcf80dc33721d54L,
			0xd1b71758e219652bL, 0xd3c36113404ea4a9L,
			0x83126e978d4fdf3bL, 0x645a1cac083126eaL,
			0xa3d70a3d70a3d70aL, 0x3d70a3d70a3d70a4L,
			0xccccccccccccccccL, 0xcccccccccccccccdL,
			0x8000000000000000L, 0x0000000000000000L,
			0xa000000000000000L, 0x0000000000000000L,
			0xc800000000000000L, 0x0000000000000000L,
			0xfa00000000000000L, 0x0000000000000000L,
			0x9c40000000000000L, 0x0000000000000000L,
			0xc350000000000000L, 0x0000000000000000L,
			0xf424000000000000L, 0x0000000000000000L,
			0x9896800000000000L, 0x0000000000000000L,
			0xbebc200000000000L, 0x0000000000000000L,
			0xee6b280000000000L, 0x0000000000000000L,
			0x9502f90000000000L, 0x0000000000000000L,
			0xba43b74000000000L, 0x0000000000000000L,
			0xe8d4a51000000000L, 0x0000000000000000L,
			0x9184e72a00000000L, 0x0000000000000000L,
			0xb5e620f480000000L, 0x0000000000000000L,
			0xe35fa931a0000000L, 0x0000000000000000L,
			0x8e1bc9bf04000000L, 0x0000000000000000L,
			0xb1a2bc2ec5000000L, 0x0000000000000000L,
			0xde0b6b3a76400000L, 0x0000000000000000L,
			0x8ac7230489e80000L, 0x0000000000000000L,
			0xad78ebc5ac620000L, 0x0000000000000000L,
			0xd8d726b7177a8000L, 0x0000000000000000L,
			0x878678326eac9000L, 0x0000000000000000L,
			0xa968163f0a57b400L, 0x0000000000000000L,
			0xd3c21bcecceda100L, 0x0000000000000000L,
			0x84595161401484a0L, 0x0000000000000000L,
			0xa56fa5b99019a5c8L, 0x0000000000000000L,
			0xcecb8f27f4200f3aL, 0x0000000000000000L,
			0x813f3978f8940984L, 0x4000000000000000L,
			0xa18f07d736b90be5L, 0x5000000000000000L,
			0xc9f2c9cd04674edeL, 0xa400000000000000L,
			0xfc6f7c4045812296L, 0x4d00000000000000L,
			0x9dc5ada82b70b59dL, 0xf020000000000000L,
			0xc5371912364ce305L, 0x6c28000000000000L,
			0xf684df56c3e01bc6L, 0xc732000000000000L,
			0x9a130b963a6c115cL, 0x3c7f400000000000L,
			0xc097ce7bc90715b3L, 0x4b9f100000000000L,
			0xf0bdc21abb48db20L, 0x1e86d40000000000L,
			0x96769950b50d88f4L, 0x1314448000000000L,
			0xbc143fa4e250eb31L, 0x17d955a000000000L,
			0xeb194f8e1ae525fdL, 0x5dcfab0800000000L,
			0x92efd1b8d0cf37beL, 0x5aa1cae500000000L,
			0xb7abc627050305adL, 0xf14a3d9e40000000L,
			0xe596b7b0c643c719L, 0x6d9ccd05d0000000L,
			0x8f7e32ce7bea5c6fL, 0xe4820023a2000000L,
			0xb35dbf821ae4f38bL, 0xdda2802c8a800000L,
			0xe0352f62a19e306eL, 0xd50b2037ad200000L,
			0x8c213d9da502de45L, 0x4526f422cc340000L,
			0xaf298d050e4395d6L, 0x9670b12b7f410000L,
			0xdaf3f04651d47b4cL, 0x3c0cdd765f114000L,
			0x88d8762bf324cd0fL, 0xa5880a69fb6ac800L,
			0xab0e93b6efee0053L, 0x8eea0d047a457a00L,
			0xd5d238a4abe98068L, 0x72a4904598d6d880L,
			0x85a36366eb71f041L, 0x47a6da2b7f864750L,
			0xa70c3c40a64e6c51L, 0x999090b65f67d924L,
			0xd0cf4b50cfe20765L, 0xfff4b4e3f741cf6dL,
			0x82818f1281ed449fL, 0xbff8f10e7a8921a5L,
			0xa321f2d7226895c7L, 0xaff72d52192b6a0eL,
			0xcbea6f8ceb02bb39L, 0x9bf4f8a69f764491L,
			0xfee50b7025c36a08L, 0x02f236d04753d5b5L,
			0x9f4f2726179a2245L, 0x01d762422c946591L,
			0xc722f0ef9d80aad6L, 0x424d3ad2b7b97ef6L,
			0xf8ebad2b84e0d58bL, 0xd2e0898765a7deb3L,
			0x9b934c3b330c8577L, 0x63cc55f49f88eb30L,
			0xc2781f49ffcfa6d5L, 0x3cbf6b71c76b25fcL,
			0xf316271c7fc3908aL, 0x8bef464e3945ef7bL,
			0x97edd871cfda3a56L, 0x97758bf0e3cbb5adL,
			0xbde94e8e43d0c8ecL, 0x3d52eeed1cbea318L,
			0xed63a231d4c4fb27L, 0x4ca7aaa863ee4bdeL,
			0x945e455f24fb1cf8L, 0x8fe8caa93e74ef6bL,
			0xb975d6b6ee39e436L, 0xb3e2fd538e122b45L,
			0xe7d34c64a9c85d44L, 0x60dbbca87196b617L,
			0x90e40fbeea1d3a4aL, 0xbc8955e946fe31ceL,
			0xb51d13aea4a488ddL, 0x6babab6398bdbe42L,
			0xe264589a4dcdab14L, 0xc696963c7eed2dd2L,
			0x8d7eb76070a08aecL, 0xfc1e1de5cf543ca3L,
			0xb0de65388cc8ada8L, 0x3b25a55f43294bccL,
			0xdd15fe86affad912L, 0x49ef0eb713f39ebfL,
			0x8a2dbf142dfcc7abL, 0x6e3569326c784338L,
			0xacb92ed9397bf996L, 0x49c2c37f07965405L,
			0xd7e77a8f87daf7fbL, 0xdc33745ec97be907L,
			0x86f0ac99b4e8dafdL, 0x69a028bb3ded71a4L,
			0xa8acd7c0222311bcL, 0xc40832ea0d68ce0dL,
			0xd2d80db02aabd62bL, 0xf50a3fa490c30191L,
			0x83c7088e1aab65dbL, 0x792667c6da79e0fbL,
			0xa4b8cab1a1563f52L, 0x577001b891185939L,
			0xcde6fd5e09abcf26L, 0xed4c0226b55e6f87L,
			0x80b05e5ac60b6178L, 0x544f8158315b05b5L,
			0xa0dc75f1778e39d6L, 0x696361ae3db1c722L,
			0xc913936dd571c84cL, 0x03bc3a19cd1e38eaL,
			0xfb5878494ace3a5fL, 0x04ab48a04065c724L,
			0x9d174b2dcec0e47bL, 0x62eb0d64283f9c77L,
			0xc45d1df942711d9aL, 0x3ba5d0bd324f8395L,
			0xf5746577930d6500L, 0xca8f44ec7ee3647aL,
			0x9968bf6abbe85f20L, 0x7e998b13cf4e1eccL,
			0xbfc2ef456ae276e8L, 0x9e3fedd8c321a67fL,
			0xefb3ab16c59b14a2L, 0xc5cfe94ef3ea101fL,
			0x95d04aee3b80ece5L, 0xbba1f1d158724a13L,
			0xbb445da9ca61281fL, 0x2a8a6e45ae8edc98L,
			0xea1575143cf97226L, 0xf52d09d71a3293beL,
			0x924d692ca61be758L, 0x593c2626705f9c57L,
			0xb6e0c377cfa2e12eL, 0x6f8b2fb00c77836dL,
			0xe498f455c38b997aL, 0x0b6dfb9c0f956448L,
			0x8edf98b59a373fecL, 0x4724bd4189bd5eadL,
			0xb2977ee300c50fe7L, 0x58edec91ec2cb658L,
			0xdf3d5e9bc0f653e1L, 0x2f2967b66737e3eeL,
			0x8b865b215899f46cL, 0xbd79e0d20082ee75L,
			0xae67f1e9aec07187L, 0xecd8590680a3aa12L,
			0xda01ee641a708de9L, 0xe80e6f4820cc9496L,
			0x884134fe908658b2L, 0x3109058d147fdcdeL,
			0xaa51823e34a7eedeL, 0xbd4b46f0599fd416L,
			0xd4e5e2cdc1d1ea96L, 0x6c9e18ac7007c91bL,
			0x850fadc09923329eL, 0x03e2cf6bc604ddb1L,
			0xa6539930bf6bff45L, 0x84db8346b786151dL,
			0xcfe87f7cef46ff16L, 0xe612641865679a64L,
			0x81f14fae158c5f6eL, 0x4fcb7e8f3f60c07fL,
			0xa26da3999aef7749L, 0xe3be5e330f38f09eL,
			0xcb090c8001ab551cL, 0x5cadf5bfd3072cc6L,
			0xfdcb4fa002162a63L, 0x73d9732fc7c8f7f7L,
			0x9e9f11c4014dda7eL, 0x2867e7fddcdd9afbL,
			0xc646d63501a1511dL, 0xb281e1fd541501b9L,
			0xf7d88bc24209a565L, 0x1f225a7ca91a4227L,
			0x9ae757596946075fL, 0x3375788de9b06959L,
			0xc1a12d2fc3978937L, 0x0052d6b1641c83afL,
			0xf209787bb47d6b84L, 0xc0678c5dbd23a49bL,
			0x9745eb4d50ce6332L, 0xf840b7ba963646e1L,
			0xbd176620a501fbffL, 0xb650e5a93bc3d899L,
			0xec5d3fa8ce427affL, 0xa3e51f138ab4cebfL,
			0x93ba47c980e98cdfL, 0xc66f336c36b10138L,
			0xb8a8d9bbe123f017L, 0xb80b0047445d4185L,
			0xe6d3102ad96cec1dL, 0xa60dc059157491e6L,
			0x9043ea1ac7e41392L, 0x87c89837ad68db30L,
			0xb454e4a179dd1877L, 0x29babe4598c311fcL,
			0xe16a1dc9d8545e94L, 0xf4296dd6fef3d67bL,
			0x8ce2529e2734bb1dL, 0x1899e4a65f58660dL,
			0xb01ae745b101e9e4L, 0x5ec05dcff72e7f90L,
			0xdc21a1171d42645dL, 0x76707543f4fa1f74L,
			0x899504ae72497ebaL, 0x6a06494a791c53a9L,
			0xabfa45da0edbde69L, 0x0487db9d17636893L,
			0xd6f8d7509292d603L, 0x45a9d2845d3c42b7L,
			0x865b86925b9bc5c2L, 0x0b8a2392ba45a9b3L,
			0xa7f26836f282b732L, 0x8e6cac7768d7141fL,
			0xd1ef0244af2364ffL, 0x3207d795430cd927L,
			0x8335616aed761f1fL, 0x7f44e6bd49e807b9L,
			0xa402b9c5a8d3a6e7L, 0x5f16206c9c6209a7L,
			0xcd036837130890a1L, 0x36dba887c37a8c10L,
			0x802221226be55a64L, 0xc2494954da2c978aL,
			0xa02aa96b06deb0fdL, 0xf2db9baa10b7bd6dL,
			0xc83553c5c8965d3dL, 0x6f92829494e5acc8L,
			0xfa42a8b73abbf48cL, 0xcb772339ba1f17faL,
			0x9c69a97284b578d7L, 0xff2a760414536efcL,
			0xc38413cf25e2d70dL, 0xfef5138519684abbL,
			0xf46518c2ef5b8cd1L, 0x7eb258665fc25d6aL,
			0x98bf2f79d5993802L, 0xef2f773ffbd97a62L,
			0xbeeefb584aff8603L, 0xaafb550ffacfd8fbL,
			0xeeaaba2e5dbf6784L, 0x95ba2a53f983cf39L,
			0x952ab45cfa97a0b2L, 0xdd945a747bf26184L,
			0xba756174393d88dfL, 0x94f971119aeef9e5L,
			0xe912b9d1478ceb17L, 0x7a37cd5601aab85eL,
			0x91abb422ccb812eeL, 0xac62e055c10ab33bL,
			0xb616a12b7fe617aaL, 0x577b986b314d600aL,
			0xe39c49765fdf9d94L, 0xed5a7e85fda0b80cL,
			0x8e41ade9fbebc27dL, 0x14588f13be847308L,
			0xb1d219647ae6b31cL, 0x596eb2d8ae258fc9L,
			0xde469fbd99a05fe3L, 0x6fca5f8ed9aef3bcL,
			0x8aec23d680043beeL, 0x25de7bb9480d5855L,
			0xada72ccc20054ae9L, 0xaf561aa79a10ae6bL,
			0xd910f7ff28069da4L, 0x1b2ba1518094da05L,
			0x87aa9aff79042286L, 0x90fb44d2f05d0843L,
			0xa99541bf57452b28L, 0x353a1607ac744a54L,
			0xd3fa922f2d1675f2L, 0x42889b8997915ce9L,
			0x847c9b5d7c2e09b7L, 0x69956135febada12L,
			0xa59bc234db398c25L, 0x43fab9837e699096L,
			0xcf02b2c21207ef2eL, 0x94f967e45e03f4bcL,
			0x8161afb94b44f57dL, 0x1d1be0eebac278f6L,
			0xa1ba1ba79e1632dcL, 0x6462d92a69731733L,
			0xca28a291859bbf93L, 0x7d7b8f7503cfdcffL,
			0xfcb2cb35e702af78L, 0x5cda735244c3d43fL,
			0x9defbf01b061adabL, 0x3a0888136afa64a8L,
			0xc56baec21c7a1916L, 0x088aaa1845b8fdd1L,
			0xf6c69a72a3989f5bL, 0x8aad549e57273d46L,
			0x9a3c2087a63f6399L, 0x36ac54e2f678864cL,
			0xc0cb28a98fcf3c7fL, 0x84576a1bb416a7deL,
			0xf0fdf2d3f3c30b9fL, 0x656d44a2a11c51d6L,
			0x969eb7c47859e743L, 0x9f644ae5a4b1b326L,
			0xbc4665b596706114L, 0x873d5d9f0dde1fefL,
			0xeb57ff22fc0c7959L, 0xa90cb506d155a7ebL,
			0x9316ff75dd87cbd8L, 0x09a7f12442d588f3L,
			0xb7dcbf5354e9beceL, 0x0c11ed6d538aeb30L,
			0xe5d3ef282a242e81L, 0x8f1668c8a86da5fbL,
			0x8fa475791a569d10L, 0xf96e017d694487bdL,
			0xb38d92d760ec4455L, 0x37c981dcc395a9adL,
			0xe070f78d3927556aL, 0x85bbe253f47b1418L,
			0x8c469ab843b89562L, 0x93956d7478ccec8fL,
			0xaf58416654a6babbL, 0x387ac8d1970027b3L,
			0xdb2e51bfe9d0696aL, 0x06997b05fcc0319fL,
			0x88fcf317f22241e2L, 0x441fece3bdf81f04L,
			0xab3c2fddeeaad25aL, 0xd527e81cad7626c4L,
			0xd60b3bd56a5586f1L, 0x8a71e223d8d3b075L,
			0x85c7056562757456L, 0xf6872d5667844e4aL,
			0xa738c6bebb12d16cL, 0xb428f8ac016561dcL,
			0xd106f86e69d785c7L, 0xe13336d701beba53L,
			0x82a45b450226b39cL, 0xecc0024661173474L,
			0xa34d721642b06084L, 0x27f002d7f95d0191L,
			0xcc20ce9bd35c78a5L, 0x31ec038df7b441f5L,
			0xff290242c83396ceL, 0x7e67047175a15272L,
			0x9f79a169bd203e41L, 0x0f0062c6e984d387L,
			0xc75809c42c684dd1L, 0x52c07b78a3e60869L,
			0xf92e0c3537826145L, 0xa7709a56ccdf8a83L,
			0x9bbcc7a142b17ccbL, 0x88a66076400bb692L,
			0xc2abf989935ddbfeL, 0x6acff893d00ea436L,
			0xf356f7ebf83552feL, 0x0583f6b8c4124d44L,
			0x98165af37b2153deL, 0xc3727a337a8b704bL,
			0xbe1bf1b059e9a8d6L, 0x744f18c0592e4c5dL,
			0xeda2ee1c7064130cL, 0x1162def06f79df74L,
			0x9485d4d1c63e8be7L, 0x8addcb5645ac2ba9L,
			0xb9a74a0637ce2ee1L, 0x6d953e2bd7173693L,
			0xe8111c87c5c1ba99L, 0xc8fa8db6ccdd0438L,
			0x910ab1d4db9914a0L, 0x1d9c9892400a22a3L,
			0xb54d5e4a127f59c8L, 0x2503beb6d00cab4cL,
			0xe2a0b5dc971f303aL, 0x2e44ae64840fd61eL,
			0x8da471a9de737e24L, 0x5ceaecfed289e5d3L,
			0xb10d8e1456105dadL, 0x7425a83e872c5f48L,
			0xdd50f1996b947518L, 0xd12f124e28f7771aL,
			0x8a5296ffe33cc92fL, 0x82bd6b70d99aaa70L,
			0xace73cbfdc0bfb7bL, 0x636cc64d1001550cL,
			0xd8210befd30efa5aL, 0x3c47f7e05401aa4fL,
			0x8714a775e3e95c78L, 0x65acfaec34810a72L,
			0xa8d9d1535ce3b396L, 0x7f1839a741a14d0eL,
			0xd31045a8341ca07cL, 0x1ede48111209a051L,
			0x83ea2b892091e44dL, 0x934aed0aab460433L,
			0xa4e4b66b68b65d60L, 0xf81da84d56178540L,
			0xce1de40642e3f4b9L, 0x36251260ab9d668fL,
			0x80d2ae83e9ce78f3L, 0xc1d72b7c6b42601aL,
			0xa1075a24e4421730L, 0xb24cf65b8612f820L,
			0xc94930ae1d529cfcL, 0xdee033f26797b628L,
			0xfb9b7cd9a4a7443cL, 0x169840ef017da3b2L,
			0x9d412e0806e88aa5L, 0x8e1f289560ee864fL,
			0xc491798a08a2ad4eL, 0xf1a6f2bab92a27e3L,
			0xf5b5d7ec8acb58a2L, 0xae10af696774b1dcL,
			0x9991a6f3d6bf1765L, 0xacca6da1e0a8ef2aL,
			0xbff610b0cc6edd3fL, 0x17fd090a58d32af4L,
			0xeff394dcff8a948eL, 0xddfc4b4cef07f5b1L,
			0x95f83d0a1fb69cd9L, 0x4abdaf101564f98fL,
			0xbb764c4ca7a4440fL, 0x9d6d1ad41abe37f2L,
			0xea53df5fd18d5513L, 0x84c86189216dc5eeL,
			0x92746b9be2f8552cL, 0x32fd3cf5b4e49bb5L,
			0xb7118682dbb66a77L, 0x3fbc8c33221dc2a2L,
			0xe4d5e82392a40515L, 0x0fabaf3feaa5334bL,
			0x8f05b1163ba6832dL, 0x29cb4d87f2a7400fL,
			0xb2c71d5bca9023f8L, 0x743e20e9ef511013L,
			0xdf78e4b2bd342cf6L, 0x914da9246b255417L,
			0x8bab8eefb6409c1aL, 0x1ad089b6c2f7548fL,
			0xae9672aba3d0c320L, 0xa184ac2473b529b2L,
			0xda3c0f568cc4f3e8L, 0xc9e5d72d90a2741fL,
			0x8865899617fb1871L, 0x7e2fa67c7a658893L,
			0xaa7eebfb9df9de8dL, 0xddbb901b98feeab8L,
			0xd51ea6fa85785631L, 0x552a74227f3ea566L,
			0x8533285c936b35deL, 0xd53a88958f872760L,
			0xa67ff273b8460356L, 0x8a892abaf368f138L,
			0xd01fef10a657842cL, 0x2d2b7569b0432d86L,
			0x8213f56a67f6b29bL, 0x9c3b29620e29fc74L,
			0xa298f2c501f45f42L, 0x8349f3ba91b47b90L,
			0xcb3f2f7642717713L, 0x241c70a936219a74L,
			0xfe0efb53d30dd4d7L, 0xed238cd383aa0111L,
			0x9ec95d1463e8a506L, 0xf4363804324a40abL,
			0xc67bb4597ce2ce48L, 0xb143c6053edcd0d6L,
			0xf81aa16fdc1b81daL, 0xdd94b7868e94050bL,
			0x9b10a4e5e9913128L, 0xca7cf2b4191c8327L,
			0xc1d4ce1f63f57d72L, 0xfd1c2f611f63a3f1L,
			0xf24a01a73cf2dccfL, 0xbc633b39673c8cedL,
			0x976e41088617ca01L, 0xd5be0503e085d814L,
			0xbd49d14aa79dbc82L, 0x4b2d8644d8a74e19L,
			0xec9c459d51852ba2L, 0xddf8e7d60ed1219fL,
			0x93e1ab8252f33b45L, 0xcabb90e5c942b504L,
			0xb8da1662e7b00a17L, 0x3d6a751f3b936244L,
			0xe7109bfba19c0c9dL, 0x0cc512670a783ad5L,
			0x906a617d450187e2L, 0x27fb2b80668b24c6L,
			0xb484f9dc9641e9daL, 0xb1f9f660802dedf7L,
			0xe1a63853bbd26451L, 0x5e7873f8a0396974L,
			0x8d07e33455637eb2L, 0xdb0b487b6423e1e9L,
			0xb049dc016abc5e5fL, 0x91ce1a9a3d2cda63L,
			0xdc5c5301c56b75f7L, 0x7641a140cc7810fcL,
			0x89b9b3e11b6329baL, 0xa9e904c87fcb0a9eL,
			0xac2820d9623bf429L, 0x546345fa9fbdcd45L,
			0xd732290fbacaf133L, 0xa97c177947ad4096L,
			0x867f59a9d4bed6c0L, 0x49ed8eabcccc485eL,
			0xa81f301449ee8c70L, 0x5c68f256bfff5a75L,
			0xd226fc195c6a2f8cL, 0x73832eec6fff3112L,
			0x83585d8fd9c25db7L, 0xc831fd53c5ff7eacL,
			0xa42e74f3d032f525L, 0xba3e7ca8b77f5e56L,
			0xcd3a1230c43fb26fL, 0x28ce1bd2e55f35ecL,
			0x80444b5e7aa7cf85L, 0x7980d163cf5b81b4L,
			0xa0555e361951c366L, 0xd7e105bcc3326220L,
			0xc86ab5c39fa63440L, 0x8dd9472bf3fefaa8L,
			0xfa856334878fc150L, 0xb14f98f6f0feb952L,
			0x9c935e00d4b9d8d2L, 0x6ed1bf9a569f33d4L,
			0xc3b8358109e84f07L, 0x0a862f80ec4700c9L,
			0xf4a642e14c6262c8L, 0xcd27bb612758c0fbL,
			0x98e7e9cccfbd7dbdL, 0x8038d51cb897789dL,
			0xbf21e44003acdd2cL, 0xe0470a63e6bd56c4L,
			0xeeea5d5004981478L, 0x1858ccfce06cac75L,
			0x95527a5202df0ccbL, 0x0f37801e0c43ebc9L,
			0xbaa718e68396cffdL, 0xd30560258f54e6bbL,
			0xe950df20247c83fdL, 0x47c6b82ef32a206aL,
			0x91d28b7416cdd27eL, 0x4cdc331d57fa5442L,
			0xb6472e511c81471dL, 0xe0133fe4adf8e953L,
			0xe3d8f9e563a198e5L, 0x58180fddd97723a7L,
			0x8e679c2f5e44ff8fL, 0x570f09eaa7ea7649L,
			0xb201833b35d63f73L, 0x2cd2cc6551e513dbL,
			0xde81e40a034bcf4fL, 0xf8077f7ea65e58d2L,
			0x8b112e86420f6191L, 0xfb04afaf27faf783L,
			0xadd57a27d29339f6L, 0x79c5db9af1f9b564L,
			0xd94ad8b1c7380874L, 0x18375281ae7822bdL,
			0x87cec76f1c830548L, 0x8f2293910d0b15b6L,
			0xa9c2794ae3a3c69aL, 0xb2eb3875504ddb23L,
			0xd433179d9c8cb841L, 0x5fa60692a46151ecL,
			0x849feec281d7f328L, 0xdbc7c41ba6bcd334L,
			0xa5c7ea73224deff3L, 0x12b9b522906c0801L,
			0xcf39e50feae16befL, 0xd768226b34870a01L,
			0x81842f29f2cce375L, 0xe6a1158300d46641L,
			0xa1e53af46f801c53L, 0x60495ae3c1097fd1L,
			0xca5e89b18b602368L, 0x385bb19cb14bdfc5L,
			0xfcf62c1dee382c42L, 0x46729e03dd9ed7b6L,
			0x9e19db92b4e31ba9L, 0x6c07a2c26a8346d2L,
			0xc5a05277621be293L, 0xc7098b7305241886L,
			0xf70867153aa2db38L, 0xb8cbee4fc66d1ea8L,
	};

	public static final int MAX_FLOAT_BYTES = 15; // 4 + (DECIMAL_SIGNIFICAND_DIGITS=9) + (DECIMAL_EXPONENT_DIGITS=2);
	public static final int MAX_DOUBLE_BYTES = 24; // 4 + (DECIMAL_SIGNIFICAND_DIGITS=17) + (DECIMAL_EXPONENT_DIGITS=3);

	private static final Unsafe u = Json.getUnsafe();

	private static void putByte(byte[] buf, int p, byte v) {
		u.putByte(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, v);
	}

	private static void putShort(byte[] buf, int p, short v) {
		u.putShort(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, v);
	}

	private static void putInt(byte[] buf, int p, int v) {
		u.putInt(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, v);
	}

	private static void putLong(byte[] buf, int p, long v) {
		u.putLong(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, v);
	}

	private static void putDot(byte[] buf, int p) {
		u.putByte(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, (byte)'.');
	}

	private static void putDigit1(byte[] buf, int p, int i) {
		u.putByte(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, u.getByte(DIGITES_LUT, Unsafe.ARRAY_BYTE_BASE_OFFSET + i));
	}

	private static void putDigit2(byte[] buf, int p, int i) {
		u.putShort(buf, Unsafe.ARRAY_BYTE_BASE_OFFSET + p, u.getShort(DIGITES_LUT, Unsafe.ARRAY_BYTE_BASE_OFFSET + i));
	}

	public static int writeFloat(final float f, final byte[] buf, int pos) {
		if (buf.length - pos < MAX_FLOAT_BYTES)
			throw new ArrayIndexOutOfBoundsException();
		final int SIGNIFICAND_BITS = 23;
		final int EXPONENT_BITS = 8;
		final int MIN_EXPONENT = -126; // MAX_EXPONENT = 127
		final int EXPONENT_BIAS = -127;
		final int MIN_K = -31;
		final int CACHE_BITS = 64;
		final int KAPPA = 1;
		final int DIVIDE_MAGIC_NUMBER = 6554; // KAPPA == 1 ? 6554 : 656;
		final int BIG_DIVISOR = 100;
		final int SMALL_DIVISOR = 10;
		final int SHORTER_INTERVAL_TIE_LOWER_THRESHOLD = -35;
		final int u = Float.floatToRawIntBits(f);
		final int u2 = u << 1;
		if (u2 == 0) {
			if (u < 0) {
				putInt(buf, pos, 0x302e302d); // "-0.0"
				pos += 4;
			} else
				putByte(buf, pos++, (byte)'0');
			return pos;
		}
		int num = u & ((1 << SIGNIFICAND_BITS) - 1);
		int exp = u2 >>> (SIGNIFICAND_BITS + 1);
		if (exp == (1 << EXPONENT_BITS) - 1) {
			if (num == 0) {
				if (u < 0)
					putByte(buf, pos++, (byte)'-');
				putLong(buf, pos, 0x7974696e_69666e49L); // "Infinity"
				pos += 8;
			} else {
				putShort(buf, pos, (short)0x614e); // "NaN"
				putByte(buf, pos + 2, (byte)'N');
				pos += 3;
			}
			return pos;
		}
		if (u < 0)
			putByte(buf, pos++, (byte)'-');
		do {
			final int num2;
			if (exp == 0) {
				exp = MIN_EXPONENT - SIGNIFICAND_BITS;
				num2 = num << 1;
			} else if (num != 0) {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				num2 = (num << 1) + (1 << (SIGNIFICAND_BITS + 1));
			} else {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				final int minusK = (exp * 631305 - 261663) >> 21;
				final int beta = exp + ((-minusK * 1741647) >> 19);
				final long cache = FLOAT_TABLE[-minusK - MIN_K];
				final int xi = (int)((cache - (cache >>> (SIGNIFICAND_BITS + 2))) >>>
						(CACHE_BITS - SIGNIFICAND_BITS - 1 - beta)) + ((exp & ~1) == 2 ? 0 : 1);
				final long zi = (cache + (cache >>> (SIGNIFICAND_BITS + 1))) >>>
						(CACHE_BITS - SIGNIFICAND_BITS - 1 - beta);
				num = (int)((zi * 429496730L) >>> 32);
				if (Integer.compareUnsigned(num * 10, xi) >= 0) {
					int r = Integer.rotateRight(num * 184254097, 4);
					int s = 0;
					if (Integer.compareUnsigned(r, 429497) < 0) {
						s = 1;
						num = r;
					}
					r = Integer.rotateRight(num * 42949673, 2);
					s <<= 1;
					if (Integer.compareUnsigned(r, 42949673) < 0) {
						num = r;
						s++;
					}
					r = Integer.rotateRight(num * 1288490189, 1);
					s <<= 1;
					if (Integer.compareUnsigned(r, 429496730) < 0) {
						num = r;
						s++;
					}
					exp = minusK + s + 1;
				} else {
					num = ((int)(cache >>> (CACHE_BITS - SIGNIFICAND_BITS - 2 - beta)) + 1) >>> 1;
					if ((num & 1) != 0 && exp == SHORTER_INTERVAL_TIE_LOWER_THRESHOLD)
						num--;
					else
						num += Integer.compareUnsigned(num, xi) >>> 31;
					exp = minusK;
				}
				break;
			}
			final int minusK = ((exp * 315653) >> 20) - KAPPA;
			final int beta = exp + ((-minusK * 1741647) >> 19);
			final long cache = FLOAT_TABLE[-minusK - MIN_K];
			final int deltai = (int)(cache >>> (CACHE_BITS - 1 - beta));
			final long uu = (num2 | 1L) << (beta + 32);
			final long r2 = Math.unsignedMultiplyHigh(uu, cache);
			final int zResult = (int)(r2 >>> 32);
			num = (int)(((zResult & 0xffff_ffffL) * 1374389535) >>> 37);
			int r = zResult - num * BIG_DIVISOR;
			int s = Long.compareUnsigned(r, deltai);
			if (s < 0 && (r | (((int)r2) != 0 ? 1 : 0) | (~u & 1)) == 0) {
				num--;
				r = BIG_DIVISOR;
				s = -s;
			}
			final long r3;
			if (s > 0 || s == 0 && (((r3 = (num2 - 1L) * cache) >>> (64 - beta)) & 1) == 0
					&& ((u & 1) != 0 || (int)(r3 >>> (32 - beta)) != 0)) {
				final int dist = r - (deltai >>> 1) + (SMALL_DIVISOR >>> 1);
				final int prod = dist * DIVIDE_MAGIC_NUMBER;
				num = num * 10 + (prod >>> 16);
				if ((prod & 0xffff) < DIVIDE_MAGIC_NUMBER) {
					final long r4 = num2 * cache;
					num -= (((int)(r4 >>> (64 - beta)) ^ (dist ^ (SMALL_DIVISOR >>> 1)))
							| (num & (((int)(r4 >>> (32 - beta)) == 0) ? 1 : 0))) & 1;
				}
				exp = minusK + KAPPA;
			} else {
				r = Integer.rotateRight(num * 184254097, 4);
				s = 0;
				if (Integer.compareUnsigned(r, 429497) < 0) {
					s = 1;
					num = r;
				}
				r = Integer.rotateRight(num * 42949673, 2);
				s <<= 1;
				if (Integer.compareUnsigned(r, 42949673) < 0) {
					num = r;
					s++;
				}
				r = Integer.rotateRight(num * 1288490189, 1);
				s <<= 1;
				if (Integer.compareUnsigned(r, 429496730) < 0) {
					num = r;
					s++;
				}
				exp = minusK + s + KAPPA + 1;
			}
		} while (false);
		int p = pos;
		if (num < 1_0000_0000) {
			if (num < 1_0000) {
				if (num < 10)
					putByte(buf, p++, (byte)('0' + num));
				else {
					final int d1 = (num / 100) << 1;
					final int d2 = (num % 100) << 1;
					if (num >= 100) {
						if (num >= 1000) {
							putDigit1(buf, p++, d1); // buf[p++] = DIGITES_LUT[d1];
							putDot(buf, p++);
							putDigit1(buf, p++, d1 + 1);
						} else {
							putDigit1(buf, p++, d1 + 1);
							putDot(buf, p++);
						}
						putDigit2(buf, p, d2);
						p += 2;
					} else {
						putDigit1(buf, p++, d2);
						putDot(buf, p++);
						putDigit1(buf, p++, d2 + 1);
					}
				}
			} else { // value = bbbb_cccc
				final int b = num / 1_0000;
				final int c = num % 1_0000;
				final int d3 = (c / 100) << 1;
				final int d4 = (c % 100) << 1;
				if (num < 10_0000) {
					putByte(buf, p++, (byte)('0' + b));
					putDot(buf, p++);
				} else {
					final int d1 = (b / 100) << 1;
					final int d2 = (b % 100) << 1;
					if (num >= 100_0000) {
						if (num >= 1000_0000) {
							putDigit1(buf, p++, d1);
							putDot(buf, p++);
							putDigit1(buf, p++, d1 + 1);
						} else {
							putDigit1(buf, p++, d1 + 1);
							putDot(buf, p++);
						}
						putDigit2(buf, p, d2);
						p += 2;
					} else {
						putDigit1(buf, p++, d2);
						putDot(buf, p++);
						putDigit1(buf, p++, d2 + 1);
					}
				}
				putDigit2(buf, p, d3);
				putDigit2(buf, p + 2, d4);
				p += 4;
			}
		} else { // if (num < 10_0000_0000)
			final int v0 = num / 1_0000_0000;
			final int v1 = num % 1_0000_0000;
			final int b1 = v1 / 1_0000;
			final int c1 = v1 % 1_0000;
			final int d5 = (b1 / 100) << 1;
			final int d6 = (b1 % 100) << 1;
			final int d7 = (c1 / 100) << 1;
			final int d8 = (c1 % 100) << 1;
			putByte(buf, p++, (byte)('0' + v0));
			putDot(buf, p++);
			putDigit2(buf, p, d5);
			putDigit2(buf, p + 2, d6);
			putDigit2(buf, p + 4, d7);
			putDigit2(buf, p + 6, d8);
			p += 8;
		}
		final int d = p - pos - 2;
		if (d > 0)
			exp += d;
		if (exp != 0) {
			putByte(buf, p++, (byte)'e');
			if (exp < 0) {
				exp = -exp;
				putByte(buf, p++, (byte)'-');
			}
			if (exp < 10)
				putByte(buf, p++, (byte)('0' + exp));
			else {
				putDigit2(buf, p, exp << 1);
				p += 2;
			}
		}
		return p;
	}

	public static int writeDouble(final double f, final byte[] buf, int pos) {
		if (buf.length - pos < MAX_DOUBLE_BYTES)
			throw new ArrayIndexOutOfBoundsException();
		final int TOTAL_BITS = 64;
		final int SIGNIFICAND_BITS = 52;
		final int EXPONENT_BITS = 11;
		final int MIN_EXPONENT = -1022; // MAX_EXPONENT = 1023
		final int EXPONENT_BIAS = -1023;
		final int MIN_K = -292;
		final int KAPPA = 2;
		final int DIVIDE_MAGIC_NUMBER = 656; // KAPPA == 1 ? 6554 : 656;
		final int BIG_DIVISOR = 1000;
		final int SMALL_DIVISOR = 100;
		final int SHORTER_INTERVAL_TIE_LOWER_THRESHOLD = -77;
		final long u = Double.doubleToRawLongBits(f);
		final long u2 = u << 1;
		if (u2 == 0) {
			if (u < 0) {
				putInt(buf, pos, 0x302e302d); // "-0.0"
				pos += 4;
			} else
				putByte(buf, pos++, (byte)'0');
			return pos;
		}
		long num = u & ((1L << SIGNIFICAND_BITS) - 1);
		int exp = (int)(u2 >>> (SIGNIFICAND_BITS + 1));
		if (exp == (1 << EXPONENT_BITS) - 1) {
			if (num == 0) {
				if (u < 0)
					putByte(buf, pos++, (byte)'-');
				putLong(buf, pos, 0x7974696e_69666e49L); // "Infinity"
				pos += 8;
			} else {
				putShort(buf, pos, (short)0x614e); // "NaN"
				putByte(buf, pos + 2, (byte)'N');
				pos += 3;
			}
			return pos;
		}
		if (u < 0)
			putByte(buf, pos++, (byte)'-');
		do {
			final long num2;
			if (exp == 0) {
				exp = MIN_EXPONENT - SIGNIFICAND_BITS;
				num2 = num << 1;
			} else if (num != 0) {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				num2 = (num << 1) + (1L << (SIGNIFICAND_BITS + 1));
			} else {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				final int minusK = (exp * 631305 - 261663) >> 21;
				final int beta = exp + ((-minusK * 1741647) >> 19);
				final long cacheHigh = DOUBLE_TABLE[(-minusK - MIN_K) << 1];
				final long xi = ((cacheHigh - (cacheHigh >>> (SIGNIFICAND_BITS + 2)))
						>>> (TOTAL_BITS - SIGNIFICAND_BITS - 1 - beta)) + ((exp & ~1) == 2 ? 0 : 1);
				final long zi = (cacheHigh + (cacheHigh >>> (SIGNIFICAND_BITS + 1)))
						>>> (TOTAL_BITS - SIGNIFICAND_BITS - 1 - beta);
				num = Math.unsignedMultiplyHigh(zi, 1844674407370955162L);
				if (Long.compareUnsigned(num * 10, xi) >= 0) {
					long r = Long.rotateRight(num * 0x67_074B_22E9_0E21L, 8);
					int s = 0;
					if (Long.compareUnsigned(r, 184467440738L) < 0) {
						s = 1;
						num = r;
					}
					r = Long.rotateRight(num * 0x288_CE70_3AFB_7E91L, 4);
					s <<= 1;
					if (Long.compareUnsigned(r, 1844674407370956L) < 0) {
						num = r;
						s++;
					}
					r = Long.rotateRight(num * 0x8F5C_28F5_C28F_5C29L, 2);
					s <<= 1;
					if (Long.compareUnsigned(r, 184467440737095517L) < 0) {
						num = r;
						s++;
					}
					r = Long.rotateRight(num * 0xCCCC_CCCC_CCCC_CCCDL, 1);
					s <<= 1;
					if (Long.compareUnsigned(r, 1844674407370955162L) < 0) {
						num = r;
						s++;
					}
					exp = minusK + s + 1;
				} else {
					num = ((cacheHigh >>> (TOTAL_BITS - SIGNIFICAND_BITS - 2 - beta)) + 1) >>> 1;
					if ((num & 1) != 0 && exp == SHORTER_INTERVAL_TIE_LOWER_THRESHOLD)
						num--;
					else
						num += Long.compareUnsigned(num, xi) >>> 31;
					exp = minusK;
				}
				break;
			}
			final int minusK = ((exp * 315653) >> 20) - KAPPA;
			final int beta = exp + ((-minusK * 1741647) >> 19);
			final int i = (-minusK - MIN_K) << 1;
			final long cacheHigh = DOUBLE_TABLE[i];
			final long cacheLow = DOUBLE_TABLE[i + 1];
			final long deltai = cacheHigh >>> (TOTAL_BITS - 1 - beta);
			final long uu = (num2 | 1) << beta;
			final long zLow1 = uu * cacheHigh;
			final long zLow = Math.unsignedMultiplyHigh(uu, cacheLow) + zLow1;
			final long zHigh = Math.unsignedMultiplyHigh(uu, cacheHigh) + (Long.compareUnsigned(zLow, zLow1) >>> 31);
			num = Math.unsignedMultiplyHigh(zHigh, 4722366482869645214L) >>> 8;
			long r = zHigh - num * BIG_DIVISOR;
			int s = Long.compareUnsigned(r, deltai);
			if (s < 0 && (r | (zLow != 0 ? 1 : 0) | (~u & 1)) == 0) {
				num--;
				r = BIG_DIVISOR;
				s = -s;
			}
			long num3, rHigh;
			if (s > 0 || s == 0 && (((rHigh = (num3 = num2 - 1) * cacheHigh
					+ Math.unsignedMultiplyHigh(num3, cacheLow)) >>> (64 - beta)) & 1) == 0
					&& (((rHigh << beta) | ((num3 * cacheLow) >>> (64 - beta))) != 0 || (u & 1) != 0)) {
				final long dist = r - (deltai >>> 1) + (SMALL_DIVISOR >>> 1);
				final int prod = (int)(dist * DIVIDE_MAGIC_NUMBER);
				num = num * 10 + (prod >>> 16);
				if ((prod & 0xffff) < DIVIDE_MAGIC_NUMBER) {
					rHigh = num2 * cacheHigh + Math.unsignedMultiplyHigh(num2, cacheLow);
					final long r4 = ((rHigh << beta) | ((num2 * cacheLow) >>> (64 - beta))) == 0 ? 1 : 0;
					num -= ((rHigh >>> (64 - beta)) ^ (dist ^ (SMALL_DIVISOR / 2)) | (num & r4)) & 1;
				}
				exp = minusK + KAPPA;
			} else {
				r = Long.rotateRight(num * 0x67_074B_22E9_0E21L, 8);
				s = 0;
				if (Long.compareUnsigned(r, 184467440738L) < 0) {
					s = 1;
					num = r;
				}
				r = Long.rotateRight(num * 0x288_CE70_3AFB_7E91L, 4);
				s <<= 1;
				if (Long.compareUnsigned(r, 1844674407370956L) < 0) {
					num = r;
					s++;
				}
				r = Long.rotateRight(num * 0x8F5C_28F5_C28F_5C29L, 2);
				s <<= 1;
				if (Long.compareUnsigned(r, 184467440737095517L) < 0) {
					num = r;
					s++;
				}
				r = Long.rotateRight(num * 0xCCCC_CCCC_CCCC_CCCDL, 1);
				s <<= 1;
				if (Long.compareUnsigned(r, 1844674407370955162L) < 0) {
					num = r;
					s++;
				}
				exp = minusK + s + KAPPA + 1;
			}
		} while (false);
		int p = pos;
		if (num < 1_0000_0000) {
			int v = (int)num;
			if (v < 1_0000) {
				if (v < 10)
					putByte(buf, p++, (byte)('0' + v));
				else {
					final int d1 = (v / 100) << 1;
					final int d2 = (v % 100) << 1;
					if (v >= 100) {
						if (v >= 1000) {
							putDigit1(buf, p++, d1); // buf[p++] = DIGITES_LUT[d1];
							putDot(buf, p++);
							putDigit1(buf, p++, d1 + 1);
						} else {
							putDigit1(buf, p++, d1 + 1);
							putDot(buf, p++);
						}
						putDigit2(buf, p, d2);
						p += 2;
					} else {
						putDigit1(buf, p++, d2);
						putDot(buf, p++);
						putDigit1(buf, p++, d2 + 1);
					}
				}
			} else { // value = bbbb_cccc
				final int b = v / 1_0000;
				final int c = v % 1_0000;
				final int d3 = (c / 100) << 1;
				final int d4 = (c % 100) << 1;
				if (v < 10_0000) {
					putByte(buf, p++, (byte)('0' + b));
					putDot(buf, p++);
				} else {
					final int d1 = (b / 100) << 1;
					final int d2 = (b % 100) << 1;
					if (v >= 100_0000) {
						if (v >= 1000_0000) {
							putDigit1(buf, p++, d1);
							putDot(buf, p++);
							putDigit1(buf, p++, d1 + 1);
						} else {
							putDigit1(buf, p++, d1 + 1);
							putDot(buf, p++);
						}
						putDigit2(buf, p, d2);
						p += 2;
					} else {
						putDigit1(buf, p++, d2);
						putDot(buf, p++);
						putDigit1(buf, p++, d2 + 1);
					}
				}
				putDigit2(buf, p, d3);
				putDigit2(buf, p + 2, d4);
				p += 4;
			}
		} else if (num < 1_0000_0000_0000_0000L) {
			final int v0 = (int)(num / 1_0000_0000);
			final int v1 = (int)(num % 1_0000_0000);
			final int b1 = v1 / 1_0000;
			final int c1 = v1 % 1_0000;
			final int d5 = (b1 / 100) << 1;
			final int d6 = (b1 % 100) << 1;
			final int d7 = (c1 / 100) << 1;
			final int d8 = (c1 % 100) << 1;
			if (num < 10_0000_0000L) {
				putByte(buf, p++, (byte)('0' + v0));
				putDot(buf, p++);
			} else {
				final int d4 = (v0 % 100) << 1;
				if (num >= 100_0000_0000L) {
					final int v2 = v0 / 100;
					final int d3 = (v2 % 100) << 1;
					if (num >= 1000_0000_0000L) {
						if (num >= 1_0000_0000_0000L) {
							final int v3 = v2 / 100;
							final int d2 = (v3 % 100) << 1;
							if (num >= 10_0000_0000_0000L) {
								if (num >= 100_0000_0000_0000L) {
									final int d1 = (v3 / 100 % 100) << 1;
									if (num >= 1000_0000_0000_0000L) {
										putDigit1(buf, p++, d1);
										putDot(buf, p++);
										putDigit1(buf, p++, d1 + 1);
									} else {
										putDigit1(buf, p++, d1 + 1);
										putDot(buf, p++);
									}
									putDigit2(buf, p, d2);
									p += 2;
								} else {
									putDigit1(buf, p++, d2);
									putDot(buf, p++);
									putDigit1(buf, p++, d2 + 1);
								}
							} else {
								putDigit1(buf, p++, d2 + 1);
								putDot(buf, p++);
							}
							putDigit2(buf, p, d3);
							p += 2;
						} else {
							putDigit1(buf, p++, d3);
							putDot(buf, p++);
							putDigit1(buf, p++, d3 + 1);
						}
					} else {
						putDigit1(buf, p++, d3 + 1);
						putDot(buf, p++);
					}
					putDigit2(buf, p, d4);
					p += 2;
				} else {
					putDigit1(buf, p, d4);
					putDot(buf, p + 1);
					putDigit1(buf, p + 2, d4 + 1);
					p += 3;
				}
			}
			putDigit2(buf, p, d5);
			putDigit2(buf, p + 2, d6);
			putDigit2(buf, p + 4, d7);
			putDigit2(buf, p + 6, d8);
			p += 8;
		} else { // if (value < 10_0000_0000_0000_0000L)
			final int a = (int)(num / 1_0000_0000_0000_0000L);
			num %= 1_0000_0000_0000_0000L;
			final int v0 = (int)(num / 1_0000_0000);
			final int v1 = (int)(num % 1_0000_0000);
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
			putByte(buf, p, (byte)('0' + a));
			putDot(buf, p + 1);
			putDigit2(buf, p + 2, d1);
			putDigit2(buf, p + 4, d2);
			putDigit2(buf, p + 6, d3);
			putDigit2(buf, p + 8, d4);
			putDigit2(buf, p + 10, d5);
			putDigit2(buf, p + 12, d6);
			putDigit2(buf, p + 14, d7);
			putDigit2(buf, p + 16, d8);
			p += 18;
		}
		final int d = p - pos - 2;
		if (d > 0)
			exp += d;
		if (exp != 0) {
			putByte(buf, p++, (byte)'e');
			if (exp < 0) {
				exp = -exp;
				putByte(buf, p++, (byte)'-');
			}
			if (exp < 10)
				putByte(buf, p++, (byte)('0' + exp));
			else if (exp < 100) {
				putDigit2(buf, p, exp << 1);
				p += 2;
			} else {
				putByte(buf, p, (byte)('0' + exp / 100));
				putDigit2(buf, p + 1, (exp % 100) << 1);
				p += 3;
			}
		}
		return p;
	}

	private static final long[] G = {
			0x4F0C_EDC9_5A71_8DD4L, 0x5B01_E8B0_9AA0_D1B5L, // -324
			0x7E7B_160E_F71C_1621L, 0x119C_A780_F767_B5EEL, // -323
			0x652F_44D8_C5B0_11B4L, 0x0E16_EC67_2C52_F7F2L, // -322
			0x50F2_9D7A_37C0_0E29L, 0x5812_56B8_F042_5FF5L, // -321
			0x40C2_1794_F966_71BAL, 0x79A8_4560_C035_1991L, // -320
			0x679C_F287_F570_B5F7L, 0x75DA_089A_CD21_C281L, // -319
			0x52E3_F539_9126_F7F9L, 0x44AE_6D48_A41B_0201L, // -318
			0x424F_F761_40EB_F994L, 0x36F1_F106_E9AF_34CDL, // -317
			0x6A19_8BCE_CE46_5C20L, 0x57E9_81A4_A918_547BL, // -316
			0x54E1_3CA5_71D1_E34DL, 0x2CBA_CE1D_5413_76C9L, // -315
			0x43E7_63B7_8E41_82A4L, 0x23C8_A4E4_4342_C56EL, // -314
			0x6CA5_6C58_E39C_043AL, 0x060D_D4A0_6B9E_08B0L, // -313
			0x56EA_BD13_E949_9CFBL, 0x1E71_76E6_BC7E_6D59L, // -312
			0x4588_9743_2107_B0C8L, 0x7EC1_2BEB_C9FE_BDE1L, // -311
			0x6F40_F205_01A5_E7A7L, 0x7E01_DFDF_A997_9635L, // -310
			0x5900_C19D_9AEB_1FB9L, 0x4B34_B319_5479_44F7L, // -309
			0x4733_CE17_AF22_7FC7L, 0x55C3_C27A_A9FA_9D93L, // -308
			0x71EC_7CF2_B1D0_CC72L, 0x5606_03F7_765D_C8EAL, // -307
			0x5B23_9728_8E40_A38EL, 0x7804_CFF9_2B7E_3A55L, // -306
			0x48E9_45BA_0B66_E93FL, 0x1337_0CC7_55FE_9511L, // -305
			0x74A8_6F90_123E_41FEL, 0x51F1_AE0B_BCCA_881BL, // -304
			0x5D53_8C73_41CB_67FEL, 0x74C1_5809_63D5_39AFL, // -303
			0x4AA9_3D29_016F_8665L, 0x43CD_E007_8310_FAF3L, // -302
			0x7775_2EA8_024C_0A3CL, 0x0616_333F_381B_2B1EL, // -301
			0x5F90_F220_01D6_6E96L, 0x3811_C298_F9AF_55B1L, // -300
			0x4C73_F4E6_67DE_BEDEL, 0x600E_3547_2E25_DE28L, // -299
			0x7A53_2170_A631_3164L, 0x3349_EED8_49D6_303FL, // -298
			0x61DC_1AC0_84F4_2783L, 0x42A1_8BE0_3B11_C033L, // -297
			0x4E49_AF00_6A5C_EC69L, 0x1BB4_6FE6_95A7_CCF5L, // -296
			0x7D42_B19A_43C7_E0A8L, 0x2C53_E63D_BC3F_AE55L, // -295
			0x6435_5AE1_CFD3_1A20L, 0x2376_51CA_FCFF_BEAAL, // -294
			0x502A_AF1B_0CA8_E1B3L, 0x35F8_416F_30CC_9888L, // -293
			0x4022_25AF_3D53_E7C2L, 0x5E60_3458_F3D6_E06DL, // -292
			0x669D_0918_621F_D937L, 0x4A33_86F4_B957_CD7BL, // -291
			0x5217_3A79_E819_7A92L, 0x6E8F_9F2A_2DDF_D796L, // -290
			0x41AC_2EC7_ECE1_2EDBL, 0x720C_7F54_F17F_DFABL, // -289
			0x6913_7E0C_AE35_17C6L, 0x1CE0_CBBB_1BFF_CC45L, // -288
			0x540F_980A_24F7_4638L, 0x171A_3C95_AFFF_D69EL, // -287
			0x433F_ACD4_EA5F_6B60L, 0x127B_63AA_F333_1218L, // -286
			0x6B99_1487_DD65_7899L, 0x6A5F_05DE_51EB_5026L, // -285
			0x5614_106C_B11D_FA14L, 0x5518_D17E_A7EF_7352L, // -284
			0x44DC_D9F0_8DB1_94DDL, 0x2A7A_4132_1FF2_C2A8L, // -283
			0x6E2E_2980_E2B5_BAFBL, 0x5D90_6850_331E_043FL, // -282
			0x5824_EE00_B55E_2F2FL, 0x6473_86A6_8F4B_3699L, // -281
			0x4683_F19A_2AB1_BF59L, 0x36C2_D21E_D908_F87BL, // -280
			0x70D3_1C29_DDE9_3228L, 0x579E_1CFE_280E_5A5DL, // -279
			0x5A42_7CEE_4B20_F4EDL, 0x2C7E_7D98_200B_7B7EL, // -278
			0x4835_30BE_A280_C3F1L, 0x09FE_CAE0_19A2_C932L, // -277
			0x7388_4DFD_D0CE_064EL, 0x4331_4499_C29E_0EB6L, // -276
			0x5C6D_0B31_73D8_050BL, 0x4F5A_9D47_CEE4_D891L, // -275
			0x49F0_D5C1_2979_9DA2L, 0x72AE_E439_7250_AD41L, // -274
			0x764E_22CE_A8C2_95D1L, 0x377E_39F5_83B4_4868L, // -273
			0x5EA4_E8A5_53CE_DE41L, 0x12CB_6191_3629_D387L, // -272
			0x4BB7_2084_430B_E500L, 0x756F_8140_F821_7605L, // -271
			0x7925_00D3_9E79_6E67L, 0x6F18_CECE_59CF_233CL, // -270
			0x60EA_670F_B1FA_BEB9L, 0x3F47_0BD8_47D8_E8FDL, // -269
			0x4D88_5272_F4C8_9894L, 0x329F_3CAD_0647_20CAL, // -268
			0x7C0D_50B7_EE0D_C0EDL, 0x3765_2DE1_A3A5_0143L, // -267
			0x633D_DA2C_BE71_6724L, 0x2C50_F181_4FB7_3436L, // -266
			0x4F64_AE8A_31F4_5283L, 0x3D0D_8E01_0C92_902BL, // -265
			0x7F07_7DA9_E986_EA6BL, 0x7B48_E334_E0EA_8045L, // -264
			0x659F_97BB_2138_BB89L, 0x4907_1C2A_4D88_669DL, // -263
			0x514C_7962_80FA_2FA1L, 0x20D2_7CEE_A46D_1EE4L, // -262
			0x4109_FAB5_33FB_594DL, 0x670E_CA58_838A_7F1DL, // -261
			0x680F_F788_532B_C216L, 0x0B4A_DD5A_6C10_CB62L, // -260
			0x533F_F939_DC23_01ABL, 0x22A2_4AAE_BCDA_3C4EL, // -259
			0x4299_942E_49B5_9AEFL, 0x354E_A225_63E1_C9D8L, // -258
			0x6A8F_537D_42BC_2B18L, 0x554A_9D08_9FCF_A95AL, // -257
			0x553F_75FD_CEFC_EF46L, 0x776E_E406_E63F_BAAEL, // -256
			0x4432_C4CB_0BFD_8C38L, 0x5F8B_E99F_1E99_6225L, // -255
			0x6D1E_07AB_4662_79F4L, 0x3279_75CB_6428_9D08L, // -254
			0x574B_3955_D1E8_6190L, 0x2861_2B09_1CED_4A6DL, // -253
			0x45D5_C777_DB20_4E0DL, 0x06B4_226D_B0BD_D524L, // -252
			0x6FBC_7259_5E9A_167BL, 0x2453_6A49_1AC9_5506L, // -251
			0x5963_8EAD_E548_11FCL, 0x1D0F_883A_7BD4_4405L, // -250
			0x4782_D88B_1DD3_4196L, 0x4A72_D361_FCA9_D004L, // -249
			0x726A_F411_C952_028AL, 0x43EA_EBCF_FAA9_4CD3L, // -248
			0x5B88_C341_6DDB_353BL, 0x4FEF_230C_C887_70A9L, // -247
			0x493A_35CD_F17C_2A96L, 0x0CBF_4F3D_6D39_26EEL, // -246
			0x7529_EFAF_E8C6_AA89L, 0x6132_1862_485B_717CL, // -245
			0x5DBB_2626_53D2_2207L, 0x675B_46B5_06AF_8DFDL, // -244
			0x4AFC_1E85_0FDB_4E6CL, 0x52AF_6BC4_0559_3E64L, // -243
			0x77F9_CA6E_7FC5_4A47L, 0x377F_12D3_3BC1_FD6DL, // -242
			0x5FFB_0858_6637_6E9FL, 0x45FF_4242_9634_CABDL, // -241
			0x4CC8_D379_EB5F_8BB2L, 0x6B32_9B68_782A_3BCBL, // -240
			0x7ADA_EBF6_4565_AC51L, 0x2B84_2BDA_59DD_2C77L, // -239
			0x6248_BCC5_0451_56A7L, 0x3C69_BCAE_AE4A_89F9L, // -238
			0x4EA0_9704_0374_4552L, 0x6387_CA25_583B_A194L, // -237
			0x7DCD_BE6C_D253_A21EL, 0x05A6_103B_C05F_68EDL, // -236
			0x64A4_9857_0EA9_4E7EL, 0x37B8_0CFC_99E5_ED8AL, // -235
			0x5083_AD12_7221_0B98L, 0x2C93_3D96_E184_BE08L, // -234
			0x4069_5741_F4E7_3C79L, 0x7075_CADF_1AD0_9807L, // -233
			0x670E_F203_2171_FA5CL, 0x4D89_4498_2AE7_59A4L, // -232
			0x5272_5B35_B45B_2EB0L, 0x3E07_6A13_5585_E150L, // -231
			0x41F5_15C4_9048_F226L, 0x64D2_BB42_AAD1_810DL, // -230
			0x6988_22D4_1A0E_503EL, 0x07B7_9204_4482_6815L, // -229
			0x546C_E8A9_AE71_D9CBL, 0x1FC6_0E69_D068_5344L, // -228
			0x438A_53BA_F1F4_AE3CL, 0x196B_3EBB_0D20_429DL, // -227
			0x6C10_85F7_E987_7D2DL, 0x0F11_FDF8_1500_6A94L, // -226
			0x5673_9E5F_EE05_FDBDL, 0x58DB_3193_4400_5543L, // -225
			0x4529_4B7F_F19E_6497L, 0x60AF_5ADC_3666_AA9CL, // -224
			0x6EA8_78CC_B5CA_3A8CL, 0x344B_C493_8A3D_DDC7L, // -223
			0x5886_C70A_2B08_2ED6L, 0x5D09_6A0F_A1CB_17D2L, // -222
			0x46D2_38D4_EF39_BF12L, 0x173A_BB3F_B4A2_7975L, // -221
			0x7150_5AEE_4B8F_981DL, 0x0B91_2B99_2103_F588L, // -220
			0x5AA6_AF25_093F_ACE4L, 0x0940_EFAD_B403_2AD3L, // -219
			0x4885_58EA_6DCC_8A50L, 0x0767_2624_9002_88A9L, // -218
			0x7408_8E43_E2E0_DD4CL, 0x723E_A36D_B337_410EL, // -217
			0x5CD3_A503_1BE7_1770L, 0x5B65_4F8A_F5C5_CDA5L, // -216
			0x4A42_EA68_E31F_45F3L, 0x62B7_72D5_916B_0AEBL, // -215
			0x76D1_770E_3832_0986L, 0x0458_B7BC_1BDE_77DDL, // -214
			0x5F0D_F8D8_2CF4_D46BL, 0x1D13_C630_164B_9318L, // -213
			0x4C0B_2D79_BD90_A9EFL, 0x30DC_9E8C_DEA2_DC13L, // -212
			0x79AB_7BF5_FC1A_A97FL, 0x0160_FDAE_3104_9351L, // -211
			0x6155_FCC4_C9AE_EDFFL, 0x1AB3_FE24_F403_A90EL, // -210
			0x4DDE_63D0_A158_BE65L, 0x6229_981D_9002_EDA5L, // -209
			0x7C97_061A_9BC1_30A2L, 0x69DC_2695_B337_E2A1L, // -208
			0x63AC_04E2_1634_26E8L, 0x54B0_1EDE_28F9_821BL, // -207
			0x4FBC_D0B4_DE90_1F20L, 0x43C0_18B1_BA61_34E2L, // -206
			0x7F94_8121_6419_CB67L, 0x1F99_C11C_5D68_549DL, // -205
			0x6610_674D_E9AE_3C52L, 0x4C7B_00E3_7DED_107EL, // -204
			0x51A6_B90B_2158_3042L, 0x09FC_00B5_FE57_4065L, // -203
			0x4152_2DA2_8113_59CEL, 0x3B30_0091_9845_CD1DL, // -202
			0x6883_7C37_34EB_C2E3L, 0x784C_CDB5_C06F_AE95L, // -201
			0x539C_635F_5D89_68B6L, 0x2D0A_3E2B_0059_5877L, // -200
			0x42E3_82B2_B13A_BA2BL, 0x3DA1_CB55_99E1_1393L, // -199
			0x6B05_9DEA_B52A_C378L, 0x629C_7888_F634_EC1EL, // -198
			0x559E_17EE_F755_692DL, 0x3549_FA07_2B5D_89B1L, // -197
			0x447E_798B_F911_20F1L, 0x1107_FB38_EF7E_07C1L, // -196
			0x6D97_28DF_F4E8_34B5L, 0x01A6_5EC1_7F30_0C68L, // -195
			0x57AC_20B3_2A53_5D5DL, 0x4E1E_B234_65C0_09EDL, // -194
			0x4623_4D5C_21DC_4AB1L, 0x24E5_5B5D_1E33_3B24L, // -193
			0x7038_7BC6_9C93_AAB5L, 0x216E_F894_FD1E_C506L, // -192
			0x59C6_C96B_B076_222AL, 0x4DF2_6077_30E5_6A6CL, // -191
			0x47D2_3ABC_8D2B_4E88L, 0x3E5B_805F_5A51_21F0L, // -190
			0x72E9_F794_1512_1740L, 0x63C5_9A32_2A1B_697FL, // -189
			0x5BEE_5FA9_AA74_DF67L, 0x0304_7B5B_54E2_BACCL, // -188
			0x498B_7FBA_EEC3_E5ECL, 0x0269_FC49_10B5_623DL, // -187
			0x75AB_FF91_7E06_3CACL, 0x6A43_2D41_B455_69FBL, // -186
			0x5E23_32DA_CB38_308AL, 0x21CF_5767_C377_87FCL, // -185
			0x4B4F_5BE2_3C2C_F3A1L, 0x67D9_12B9_692C_6CCAL, // -184
			0x787E_F969_F9E1_85CFL, 0x595B_5128_A847_1476L, // -183
			0x6065_9454_C7E7_9E3FL, 0x6115_DA86_ED05_A9F8L, // -182
			0x4D1E_1043_D31F_B1CCL, 0x4DAB_1538_BD9E_2193L, // -181
			0x7B63_4D39_51CC_4FADL, 0x62AB_5527_95C9_CF52L, // -180
			0x62B5_D761_0E3D_0C8BL, 0x0222_AA86_116E_3F75L, // -179
			0x4EF7_DF80_D830_D6D5L, 0x4E82_2204_DABE_992AL, // -178
			0x7E59_659A_F381_57BCL, 0x1736_9CD4_9130_F510L, // -177
			0x6514_5148_C2CD_DFC9L, 0x5F5E_E3DD_40F3_F740L, // -176
			0x50DD_0DD3_CF0B_196EL, 0x1918_B64A_9A5C_C5CDL, // -175
			0x40B0_D7DC_A5A2_7ABEL, 0x4746_F83B_AEB0_9E3EL, // -174
			0x6781_5961_0903_F797L, 0x253E_59F9_1780_FD2FL, // -173
			0x52CD_E11A_6D9C_C612L, 0x50FE_AE60_DF9A_6426L, // -172
			0x423E_4DAE_BE17_04DBL, 0x5A65_584D_7FAE_B685L, // -171
			0x69FD_4917_968B_3AF9L, 0x10A2_26E2_65E4_573BL, // -170
			0x54CA_A0DF_ABA2_9594L, 0x0D4E_8581_EB1D_1295L, // -169
			0x43D5_4D7F_BC82_1143L, 0x243E_D134_BC17_4211L, // -168
			0x6C88_7BFF_9403_4ED2L, 0x06CA_E854_6025_3682L, // -167
			0x56D3_9666_1002_A574L, 0x6BD5_86A9_E684_2B9BL, // -166
			0x4576_11EB_4002_1DF7L, 0x0977_9EEE_5203_5616L, // -165
			0x6F23_4FDE_CCD0_2FF1L, 0x5BF2_97E3_B66B_BCEFL, // -164
			0x58E9_0CB2_3D73_598EL, 0x165B_ACB6_2B89_63F3L, // -163
			0x4720_D6F4_FDF5_E13EL, 0x4516_23C4_EFA1_1CC2L, // -162
			0x71CE_24BB_2FEF_CECAL, 0x3B56_9FA1_7F68_2E03L, // -161
			0x5B0B_5095_BFF3_0BD5L, 0x15DE_E61A_CC53_5803L, // -160
			0x48D5_DA11_665C_0977L, 0x2B18_B815_7042_ACCFL, // -159
			0x7489_5CE8_A3C6_758BL, 0x5E8D_F355_806A_AE18L, // -158
			0x5D3A_B0BA_1C9E_C46FL, 0x653E_5C44_66BB_BE7AL, // -157
			0x4A95_5A2E_7D4B_D059L, 0x3765_169D_1EFC_9861L, // -156
			0x7755_5D17_2EDF_B3C2L, 0x256E_8A94_FE60_F3CFL, // -155
			0x5F77_7DAC_257F_C301L, 0x6ABE_D543_FEB3_F63FL, // -154
			0x4C5F_97BC_EACC_9C01L, 0x3BCB_DDCF_FEF6_5E99L, // -153
			0x7A32_8C61_77AD_C668L, 0x5FAC_9619_97F0_975BL, // -152
			0x61C2_09E7_92F1_6B86L, 0x7FBD_44E1_465A_12AFL, // -151
			0x4E34_D4B9_425A_BC6BL, 0x7FCA_9D81_0514_DBBFL, // -150
			0x7D21_545B_9D5D_FA46L, 0x32DD_C8CE_6E87_C5FFL, // -149
			0x641A_A9E2_E44B_2E9EL, 0x5BE4_A0A5_2539_6B32L, // -148
			0x5015_54B5_836F_587EL, 0x7CB6_E6EA_842D_EF5CL, // -147
			0x4011_1091_35F2_AD32L, 0x3092_5255_368B_25E3L, // -146
			0x6681_B41B_8984_4850L, 0x4DB6_EA21_F0DE_A304L, // -145
			0x5201_5CE2_D469_D373L, 0x57C5_881B_2718_826AL, // -144
			0x419A_B0B5_76BB_0F8FL, 0x5FD1_39AF_527A_01EFL, // -143
			0x68F7_8122_5791_B27FL, 0x4C81_F5E5_50C3_364AL, // -142
			0x53F9_341B_7941_5B99L, 0x239B_2B1D_DA35_C508L, // -141
			0x432D_C349_2DCD_E2E1L, 0x02E2_88E4_AE91_6A6DL, // -140
			0x6B7C_6BA8_4949_6B01L, 0x516A_74A1_174F_10AEL, // -139
			0x55FD_22ED_076D_EF34L, 0x4121_F6E7_45D8_DA25L, // -138
			0x44CA_8257_3924_BF5DL, 0x1A81_9252_9E47_14EBL, // -137
			0x6E10_D08B_8EA1_322EL, 0x5D9C_1D50_FD3E_87DDL, // -136
			0x580D_73A2_D880_F4F2L, 0x17B0_1773_FDCB_9FE4L, // -135
			0x4671_294F_139A_5D8EL, 0x4626_7929_97D6_1984L, // -134
			0x70B5_0EE4_EC2A_2F4AL, 0x3D0A_5B75_BFBC_F59FL, // -133
			0x5A2A_7250_BCEE_8C3BL, 0x4A6E_AF91_6630_C47FL, // -132
			0x4821_F50D_63F2_09C9L, 0x21F2_260D_EB5A_36CCL, // -131
			0x7369_8815_6CB6_760EL, 0x6983_7016_455D_247AL, // -130
			0x5C54_6CDD_F091_F80BL, 0x6E02_C011_D117_5062L, // -129
			0x49DD_23E4_C074_C66FL, 0x719B_CCDB_0DAC_404EL, // -128
			0x762E_9FD4_6721_3D7FL, 0x68F9_47C4_E2AD_33B0L, // -127
			0x5E8B_B310_5280_FDFFL, 0x6D94_396A_4EF0_F627L, // -126
			0x4BA2_F5A6_A867_3199L, 0x3E10_2DEE_A58D_91B9L, // -125
			0x7904_BC3D_DA3E_B5C2L, 0x3019_E317_6F48_E927L, // -124
			0x60D0_9697_E1CB_C49BL, 0x4014_B5AC_5907_20ECL, // -123
			0x4D73_ABAC_B4A3_03AFL, 0x4CDD_5E23_7A6C_1A57L, // -122
			0x7BEC_45E1_2104_D2B2L, 0x47C8_969F_2A46_908AL, // -121
			0x6323_6B1A_80D0_A88EL, 0x6CA0_787F_5505_406FL, // -120
			0x4F4F_88E2_00A6_ED3FL, 0x0A19_F9FF_7737_66BFL, // -119
			0x7EE5_A7D0_010B_1531L, 0x5CF6_5CCB_F1F2_3DFEL, // -118
			0x6584_8640_00D5_AA8EL, 0x172B_7D6F_F4C1_CB32L, // -117
			0x5136_D1CC_CD77_BBA4L, 0x78EF_978C_C3CE_3C28L, // -116
			0x40F8_A7D7_0AC6_2FB7L, 0x13F2_DFA3_CFD8_3020L, // -115
			0x67F4_3FBE_77A3_7F8BL, 0x3984_9906_1959_E699L, // -114
			0x5329_CC98_5FB5_FFA2L, 0x6136_E0D1_ADE1_8548L, // -113
			0x4287_D6E0_4C91_994FL, 0x00F8_B3DA_F181_376DL, // -112
			0x6A72_F166_E0E8_F54BL, 0x1B27_862B_1C01_F247L, // -111
			0x5528_C11F_1A53_F76FL, 0x2F52_D1BC_1667_F506L, // -110
			0x4420_9A7F_4843_2C59L, 0x0C42_4163_451F_F738L, // -109
			0x6D00_F732_0D38_46F4L, 0x7A03_9BD2_0833_2526L, // -108
			0x5733_F8F4_D760_38C3L, 0x7B36_1641_A028_EA85L, // -107
			0x45C3_2D90_AC4C_FA36L, 0x2F5E_7834_8020_BB9EL, // -106
			0x6F9E_AF4D_E07B_29F0L, 0x4BCA_59ED_99CD_F8FCL, // -105
			0x594B_BF71_8062_87F3L, 0x563B_7B24_7B0B_2D96L, // -104
			0x476F_CC5A_CD1B_9FF6L, 0x11C9_2F50_626F_57ACL, // -103
			0x724C_7A2A_E1C5_CCBDL, 0x02DB_7EE7_03E5_5912L, // -102
			0x5B70_61BB_E7D1_7097L, 0x1BE2_CBEC_031D_E0DCL, // -101
			0x4926_B496_530D_F3ACL, 0x164F_0989_9C17_E716L, // -100
			0x750A_BA8A_1E7C_B913L, 0x3D4B_4275_C68C_A4F0L, //  -99
			0x5DA2_2ED4_E530_940FL, 0x4AA2_9B91_6BA3_B726L, //  -98
			0x4AE8_2577_1DC0_7672L, 0x6EE8_7C74_561C_9285L, //  -97
			0x77D9_D58B_62CD_8A51L, 0x3173_FA53_BCFA_8408L, //  -96
			0x5FE1_77A2_B571_3B74L, 0x278F_FB76_30C8_69A0L, //  -95
			0x4CB4_5FB5_5DF4_2F90L, 0x1FA6_62C4_F3D3_87B3L, //  -94
			0x7ABA_32BB_C986_B280L, 0x32A3_D13B_1FB8_D91FL, //  -93
			0x622E_8EFC_A138_8ECDL, 0x0EE9_742F_4C93_E0E6L, //  -92
			0x4E8B_A596_E760_723DL, 0x58BA_C359_0A0F_E71EL, //  -91
			0x7DAC_3C24_A567_1D2FL, 0x412A_D228_1019_71C9L, //  -90
			0x6489_C9B6_EAB8_E426L, 0x00EF_0E86_7347_8E3BL, //  -89
			0x506E_3AF8_BBC7_1CEBL, 0x1A58_D86B_8F6C_71C9L, //  -88
			0x4058_2F2D_6305_B0BCL, 0x1513_E056_0C56_C16EL, //  -87
			0x66F3_7EAF_04D5_E793L, 0x3B53_0089_AD57_9BE2L, //  -86
			0x525C_6558_D0AB_1FA9L, 0x15DC_006E_2446_164FL, //  -85
			0x41E3_8447_0D55_B2EDL, 0x5E49_99F1_B69E_783FL, //  -84
			0x696C_06D8_1555_EB15L, 0x7D42_8FE9_2430_C065L, //  -83
			0x5456_6BE0_1111_88DEL, 0x3102_0CBA_835A_3384L, //  -82
			0x4378_564C_DA74_6D7EL, 0x5A68_0A2E_CF7B_5C69L, //  -81
			0x6BF3_BD47_C3ED_7BFDL, 0x770C_DD17_B25E_FA42L, //  -80
			0x565C_976C_9CBD_FCCBL, 0x1270_B0DF_C1E5_9502L, //  -79
			0x4516_DF8A_16FE_63D5L, 0x5B8D_5A4C_9B1E_10CEL, //  -78
			0x6E8A_FF43_57FD_6C89L, 0x127B_C3AD_C4FC_E7B0L, //  -77
			0x586F_329C_4664_56D4L, 0x0EC9_6957_D0CA_52F3L, //  -76
			0x46BF_5BB0_3850_4576L, 0x3F07_8779_73D5_0F29L, //  -75
			0x7132_2C4D_26E6_D58AL, 0x31A5_A58F_1FBB_4B75L, //  -74
			0x5A8E_89D7_5252_446EL, 0x5AEA_EAD8_E62F_6F91L, //  -73
			0x4872_07DF_750E_9D25L, 0x2F22_557A_51BF_8C74L, //  -72
			0x73E9_A632_54E4_2EA2L, 0x1836_EF2A_1C65_AD86L, //  -71
			0x5CBA_EB5B_771C_F21BL, 0x2CF8_BF54_E384_8AD2L, //  -70
			0x4A2F_22AF_927D_8E7CL, 0x23FA_32AA_4F9D_3BDBL, //  -69
			0x76B1_D118_EA62_7D93L, 0x5329_EAAA_18FB_92F8L, //  -68
			0x5EF4_A747_21E8_6476L, 0x0F54_BBBB_472F_A8C6L, //  -67
			0x4BF6_EC38_E7ED_1D2BL, 0x25DD_62FC_38F2_ED6CL, //  -66
			0x798B_138E_3FE1_C845L, 0x22FB_D193_8E51_7BDFL, //  -65
			0x613C_0FA4_FFE7_D36AL, 0x4F2F_DADC_71DA_C97FL, //  -64
			0x4DC9_A61D_9986_42BBL, 0x58F3_157D_27E2_3ACCL, //  -63
			0x7C75_D695_C270_6AC5L, 0x74B8_2261_D969_F7ADL, //  -62
			0x6391_7877_CEC0_556BL, 0x1093_4EB4_ADEE_5FBEL, //  -61
			0x4FA7_9393_0BCD_1122L, 0x4075_D890_8B25_1965L, //  -60
			0x7F72_85B8_12E1_B504L, 0x00BC_8DB4_11D4_F56EL, //  -59
			0x65F5_37C6_7581_5D9CL, 0x66FD_3E29_A7DD_9125L, //  -58
			0x5190_F96B_9134_4AE3L, 0x6BFD_CB54_864A_DA84L, //  -57
			0x4140_C789_40F6_A24FL, 0x6FFE_3C43_9EA2_486AL, //  -56
			0x6867_A5A8_67F1_03B2L, 0x7FFD_2D38_FDD0_73DCL, //  -55
			0x5386_1E20_5327_3628L, 0x6664_242D_97D9_F64AL, //  -54
			0x42D1_B1B3_75B8_F820L, 0x51E9_B68A_DFE1_91D5L, //  -53
			0x6AE9_1C52_55F4_C034L, 0x1CA9_2411_6635_B621L, //  -52
			0x5587_49DB_77F7_0029L, 0x63BA_8341_1E91_5E81L, //  -51
			0x446C_3B15_F992_6687L, 0x6962_029A_7EDA_B201L, //  -50
			0x6D79_F823_28EA_3DA6L, 0x0F03_375D_97C4_5001L, //  -49
			0x5794_C682_8721_CAEBL, 0x259C_2C4A_DFD0_4001L, //  -48
			0x4610_9ECE_D281_6F22L, 0x5149_BD08_B30D_0001L, //  -47
			0x701A_97B1_50CF_1837L, 0x3542_C80D_EB48_0001L, //  -46
			0x59AE_DFC1_0D72_79C5L, 0x7768_A00B_22A0_0001L, //  -45
			0x47BF_1967_3DF5_2E37L, 0x7920_8008_E880_0001L, //  -44
			0x72CB_5BD8_6321_E38CL, 0x5B67_3341_7400_0001L, //  -43
			0x5BD5_E313_8281_82D6L, 0x7C52_8F67_9000_0001L, //  -42
			0x4977_E8DC_6867_9BDFL, 0x16A8_72B9_4000_0001L, //  -41
			0x758C_A7C7_0D72_92FEL, 0x5773_EAC2_0000_0001L, //  -40
			0x5E0A_1FD2_7128_7598L, 0x45F6_5568_0000_0001L, //  -39
			0x4B3B_4CA8_5A86_C47AL, 0x04C5_1120_0000_0001L, //  -38
			0x785E_E10D_5DA4_6D90L, 0x07A1_B500_0000_0001L, //  -37
			0x604B_E73D_E483_8AD9L, 0x52E7_C400_0000_0001L, //  -36
			0x4D09_85CB_1D36_08AEL, 0x0F1F_D000_0000_0001L, //  -35
			0x7B42_6FAB_61F0_0DE3L, 0x31CC_8000_0000_0001L, //  -34
			0x629B_8C89_1B26_7182L, 0x5B0A_0000_0000_0001L, //  -33
			0x4EE2_D6D4_15B8_5ACEL, 0x7C08_0000_0000_0001L, //  -32
			0x7E37_BE20_22C0_914BL, 0x1340_0000_0000_0001L, //  -31
			0x64F9_64E6_8233_A76FL, 0x2900_0000_0000_0001L, //  -30
			0x50C7_83EB_9B5C_85F2L, 0x5400_0000_0000_0001L, //  -29
			0x409F_9CBC_7C4A_04C2L, 0x1000_0000_0000_0001L, //  -28
			0x6765_C793_FA10_079DL, 0x0000_0000_0000_0001L, //  -27
			0x52B7_D2DC_C80C_D2E4L, 0x0000_0000_0000_0001L, //  -26
			0x422C_A8B0_A00A_4250L, 0x0000_0000_0000_0001L, //  -25
			0x69E1_0DE7_6676_D080L, 0x0000_0000_0000_0001L, //  -24
			0x54B4_0B1F_852B_DA00L, 0x0000_0000_0000_0001L, //  -23
			0x43C3_3C19_3756_4800L, 0x0000_0000_0000_0001L, //  -22
			0x6C6B_935B_8BBD_4000L, 0x0000_0000_0000_0001L, //  -21
			0x56BC_75E2_D631_0000L, 0x0000_0000_0000_0001L, //  -20
			0x4563_9182_44F4_0000L, 0x0000_0000_0000_0001L, //  -19
			0x6F05_B59D_3B20_0000L, 0x0000_0000_0000_0001L, //  -18
			0x58D1_5E17_6280_0000L, 0x0000_0000_0000_0001L, //  -17
			0x470D_E4DF_8200_0000L, 0x0000_0000_0000_0001L, //  -16
			0x71AF_D498_D000_0000L, 0x0000_0000_0000_0001L, //  -15
			0x5AF3_107A_4000_0000L, 0x0000_0000_0000_0001L, //  -14
			0x48C2_7395_0000_0000L, 0x0000_0000_0000_0001L, //  -13
			0x746A_5288_0000_0000L, 0x0000_0000_0000_0001L, //  -12
			0x5D21_DBA0_0000_0000L, 0x0000_0000_0000_0001L, //  -11
			0x4A81_7C80_0000_0000L, 0x0000_0000_0000_0001L, //  -10
			0x7735_9400_0000_0000L, 0x0000_0000_0000_0001L, //   -9
			0x5F5E_1000_0000_0000L, 0x0000_0000_0000_0001L, //   -8
			0x4C4B_4000_0000_0000L, 0x0000_0000_0000_0001L, //   -7
			0x7A12_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -6
			0x61A8_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -5
			0x4E20_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -4
			0x7D00_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -3
			0x6400_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -2
			0x5000_0000_0000_0000L, 0x0000_0000_0000_0001L, //   -1
			0x4000_0000_0000_0000L, 0x0000_0000_0000_0001L, //    0
			0x6666_6666_6666_6666L, 0x3333_3333_3333_3334L, //    1
			0x51EB_851E_B851_EB85L, 0x0F5C_28F5_C28F_5C29L, //    2
			0x4189_374B_C6A7_EF9DL, 0x5916_872B_020C_49BBL, //    3
			0x68DB_8BAC_710C_B295L, 0x74F0_D844_D013_A92BL, //    4
			0x53E2_D623_8DA3_C211L, 0x43F3_E037_0CDC_8755L, //    5
			0x431B_DE82_D7B6_34DAL, 0x698F_E692_70B0_6C44L, //    6
			0x6B5F_CA6A_F2BD_215EL, 0x0F4C_A41D_811A_46D4L, //    7
			0x55E6_3B88_C230_E77EL, 0x3F70_834A_CDAE_9F10L, //    8
			0x44B8_2FA0_9B5A_52CBL, 0x4C5A_02A2_3E25_4C0DL, //    9
			0x6DF3_7F67_5EF6_EADFL, 0x2D5C_D103_96A2_1347L, //   10
			0x57F5_FF85_E592_557FL, 0x3DE3_DA69_454E_75D3L, //   11
			0x465E_6604_B7A8_4465L, 0x7E4F_E1ED_D10B_9175L, //   12
			0x7097_09A1_25DA_0709L, 0x4A19_697C_81AC_1BEFL, //   13
			0x5A12_6E1A_84AE_6C07L, 0x54E1_2130_67BC_E326L, //   14
			0x480E_BE7B_9D58_566CL, 0x43E7_4DC0_52FD_8285L, //   15
			0x734A_CA5F_6226_F0ADL, 0x530B_AF9A_1E62_6A6DL, //   16
			0x5C3B_D519_1B52_5A24L, 0x426F_BFAE_7EB5_21F1L, //   17
			0x49C9_7747_490E_AE83L, 0x4EBF_CC8B_9890_E7F4L, //   18
			0x760F_253E_DB4A_B0D2L, 0x4ACC_7A78_F41B_0CBAL, //   19
			0x5E72_8432_4908_8D75L, 0x223D_2EC7_29AF_3D62L, //   20
			0x4B8E_D028_3A6D_3DF7L, 0x34FD_BF05_BAF2_9781L, //   21
			0x78E4_8040_5D7B_9658L, 0x54C9_31A2_C4B7_58CFL, //   22
			0x60B6_CD00_4AC9_4513L, 0x5D6D_C14F_03C5_E0A5L, //   23
			0x4D5F_0A66_A23A_9DA9L, 0x3124_9AA5_9C9E_4D51L, //   24
			0x7BCB_43D7_69F7_62A8L, 0x4EA0_F76F_60FD_4882L, //   25
			0x6309_0312_BB2C_4EEDL, 0x254D_92BF_80CA_A068L, //   26
			0x4F3A_68DB_C8F0_3F24L, 0x1DD7_A899_33D5_4D20L, //   27
			0x7EC3_DAF9_4180_6506L, 0x62F2_A75B_8622_1500L, //   28
			0x6569_7BFA_9ACD_1D9FL, 0x025B_B916_04E8_10CDL, //   29
			0x5121_2FFB_AF0A_7E18L, 0x6849_60DE_6A53_40A4L, //   30
			0x40E7_5996_25A1_FE7AL, 0x203A_B3E5_21DC_33B6L, //   31
			0x67D8_8F56_A29C_CA5DL, 0x19F7_863B_6960_52BDL, //   32
			0x5313_A5DE_E87D_6EB0L, 0x7B2C_6B62_BAB3_7564L, //   33
			0x4276_1E4B_ED31_255AL, 0x2F56_BC4E_FBC2_C450L, //   34
			0x6A56_96DF_E1E8_3BC3L, 0x6557_93B1_92D1_3A1AL, //   35
			0x5512_124C_B4B9_C969L, 0x3779_42F4_7574_2E7BL, //   36
			0x440E_750A_2A2E_3ABAL, 0x5F94_3590_5DF6_8B96L, //   37
			0x6CE3_EE76_A9E3_912AL, 0x65B9_EF4D_6324_1289L, //   38
			0x571C_BEC5_54B6_0DBBL, 0x6AFB_25D7_8283_4207L, //   39
			0x45B0_989D_DD5E_7163L, 0x08C8_EB12_CECF_6806L, //   40
			0x6F80_F42F_C897_1BD1L, 0x5ADB_11B7_B14B_D9A3L, //   41
			0x5933_F68C_A078_E30EL, 0x157C_0E2C_8DD6_47B5L, //   42
			0x475C_C53D_4D2D_8271L, 0x5DFC_D823_A4AB_6C91L, //   43
			0x722E_0862_1515_9D82L, 0x632E_269F_6DDF_141BL, //   44
			0x5B58_06B4_DDAA_E468L, 0x4F58_1EE5_F17F_4349L, //   45
			0x4913_3890_B155_8386L, 0x72AC_E584_C132_9C3BL, //   46
			0x74EB_8DB4_4EEF_38D7L, 0x6AAE_3C07_9B84_2D2AL, //   47
			0x5D89_3E29_D8BF_60ACL, 0x5558_3006_1603_5755L, //   48
			0x4AD4_31BB_13CC_4D56L, 0x7779_C004_DE69_12ABL, //   49
			0x77B9_E92B_52E0_7BBEL, 0x258F_99A1_63DB_5111L, //   50
			0x5FC7_EDBC_424D_2FCBL, 0x37A6_1481_1CAF_740DL, //   51
			0x4C9F_F163_683D_BFD5L, 0x7951_AA00_E3BF_900BL, //   52
			0x7A99_8238_A6C9_32EFL, 0x754F_7667_D2CC_19ABL, //   53
			0x6214_682D_523A_8F26L, 0x2AA5_F853_0F09_AE22L, //   54
			0x4E76_B9BD_DB62_0C1EL, 0x5551_9375_A5A1_581BL, //   55
			0x7D8A_C2C9_5F03_4697L, 0x3BB5_B8BC_3C35_59C5L, //   56
			0x646F_023A_B269_0545L, 0x7C91_6096_9691_149EL, //   57
			0x5058_CE95_5B87_376BL, 0x16DA_B3AB_ABA7_43B2L, //   58
			0x4047_0BAA_AF9F_5F88L, 0x78AE_F622_EFB9_02F5L, //   59
			0x66D8_12AA_B298_98DBL, 0x0DE4_BD04_B2C1_9E54L, //   60
			0x5246_7555_5BAD_4715L, 0x57EA_30D0_8F01_4B76L, //   61
			0x41D1_F777_7C8A_9F44L, 0x4654_F3DA_0C01_092CL, //   62
			0x694F_F258_C744_3207L, 0x23BB_1FC3_4668_0EACL, //   63
			0x543F_F513_D29C_F4D2L, 0x4FC8_E635_D1EC_D88AL, //   64
			0x4366_5DA9_754A_5D75L, 0x263A_51C4_A7F0_AD3BL, //   65
			0x6BD6_FC42_5543_C8BBL, 0x56C3_B607_731A_AEC4L, //   66
			0x5645_969B_7769_6D62L, 0x789C_919F_8F48_8BD0L, //   67
			0x4504_787C_5F87_8AB5L, 0x46E3_A7B2_D906_D640L, //   68
			0x6E6D_8D93_CC0C_1122L, 0x3E39_0C51_5B3E_239AL, //   69
			0x5857_A476_3CD6_741BL, 0x4B60_D6A7_7C31_B615L, //   70
			0x46AC_8391_CA45_29AFL, 0x55E7_121F_968E_2B44L, //   71
			0x7114_05B6_106E_A919L, 0x0971_B698_F0E3_786DL, //   72
			0x5A76_6AF8_0D25_5414L, 0x078E_2BAD_8D82_C6BDL, //   73
			0x485E_BBF9_A41D_DCDCL, 0x6C71_BC8A_D79B_D231L, //   74
			0x73CA_C65C_39C9_6161L, 0x2D82_C744_8C2C_8382L, //   75
			0x5CA2_3849_C7D4_4DE7L, 0x3E02_3903_A356_CF9BL, //   76
			0x4A1B_603B_0643_7185L, 0x7E68_2D9C_82AB_D949L, //   77
			0x7692_3391_A39F_1C09L, 0x4A40_48FA_6AAC_8EDBL, //   78
			0x5EDB_5C74_82E5_B007L, 0x5500_3A61_EEF0_7249L, //   79
			0x4BE2_B05D_3584_8CD2L, 0x7733_61E7_F259_F507L, //   80
			0x796A_B3C8_55A0_E151L, 0x3EB8_9CA6_508F_EE71L, //   81
			0x6122_296D_114D_810DL, 0x7EFA_16EB_73A6_585BL, //   82
			0x4DB4_EDF0_DAA4_673EL, 0x3261_ABEF_8FB8_46AFL, //   83
			0x7C54_AFE7_C43A_3ECAL, 0x1D69_1318_E5F3_A44BL, //   84
			0x6376_F31F_D02E_98A1L, 0x6454_0F47_1E5C_836FL, //   85
			0x4F92_5C19_7358_7A1BL, 0x0376_729F_4B7D_35F3L, //   86
			0x7F50_935B_EBC0_C35EL, 0x38BD_8432_1261_EFEBL, //   87
			0x65DA_0F7C_BC9A_35E5L, 0x13CA_D028_0EB4_BFEFL, //   88
			0x517B_3F96_FD48_2B1DL, 0x5CA2_4020_0BC3_CCBFL, //   89
			0x412F_6612_6439_BC17L, 0x63B5_0019_A303_0A33L, //   90
			0x684B_D683_D38F_9359L, 0x1F88_0029_04D1_A9EAL, //   91
			0x536F_DECF_DC72_DC47L, 0x32D3_3354_03DA_EE55L, //   92
			0x42BF_E573_16C2_49D2L, 0x5BDC_2910_0315_8B77L, //   93
			0x6ACC_A251_BE03_A951L, 0x12F9_DB4C_D1BC_1258L, //   94
			0x5570_81DA_FE69_5440L, 0x7594_AF70_A7C9_A847L, //   95
			0x445A_017B_FEBA_A9CDL, 0x4476_F2C0_863A_ED06L, //   96
			0x6D5C_CF2C_CAC4_42E2L, 0x3A57_EACD_A391_7B3CL, //   97
			0x577D_728A_3BD0_3581L, 0x7B79_88A4_82DA_C8FDL, //   98
			0x45FD_F53B_630C_F79BL, 0x15FA_D3B6_CF15_6D97L, //   99
			0x6FFC_BB92_3814_BF5EL, 0x565E_1F8A_E4EF_15BEL, //  100
			0x5996_FC74_F9AA_32B2L, 0x11E4_E608_B725_AAFFL, //  101
			0x47AB_FD2A_6154_F55BL, 0x27EA_51A0_9284_88CCL, //  102
			0x72AC_C843_CEEE_555EL, 0x7310_829A_8407_4146L, //  103
			0x5BBD_6D03_0BF1_DDE5L, 0x4273_9BAE_D005_CDD2L, //  104
			0x4964_5735_A327_E4B7L, 0x4EC2_E2F2_4004_A4A8L, //  105
			0x756D_5855_D1D9_6DF2L, 0x4AD1_6B1D_333A_A10CL, //  106
			0x5DF1_1377_DB14_57F5L, 0x2241_227D_C295_4DA3L, //  107
			0x4B27_42C6_48DD_132AL, 0x4E9A_81FE_3544_3E1CL, //  108
			0x783E_D13D_4161_B844L, 0x175D_9CC9_EED3_9694L, //  109
			0x6032_40FD_CDE7_C69CL, 0x7917_B0A1_8BDC_7876L, //  110
			0x4CF5_00CB_0B1F_D217L, 0x1412_F3B4_6FE3_9392L, //  111
			0x7B21_9ADE_7832_E9BEL, 0x5351_85ED_7FD2_85B6L, //  112
			0x6281_48B1_F9C2_5498L, 0x42A7_9E57_9975_37C5L, //  113
			0x4ECD_D3C1_949B_76E0L, 0x3552_E512_E12A_9304L, //  114
			0x7E16_1F9C_20F8_BE33L, 0x6EEB_081E_3510_EB39L, //  115
			0x64DE_7FB0_1A60_9829L, 0x3F22_6CE4_F740_BC2EL, //  116
			0x50B1_FFC0_151A_1354L, 0x3281_F0B7_2C33_C9BEL, //  117
			0x408E_6633_4414_DC43L, 0x4201_8D5F_568F_D498L, //  118
			0x674A_3D1E_D354_939FL, 0x1CCF_4898_8A7F_BA8DL, //  119
			0x52A1_CA7F_0F76_DC7FL, 0x30A5_D3AD_3B99_620BL, //  120
			0x421B_0865_A5F8_B065L, 0x73B7_DC8A_9614_4E6FL, //  121
			0x69C4_DA3C_3CC1_1A3CL, 0x52BF_C744_2353_B0B1L, //  122
			0x549D_7B63_63CD_AE96L, 0x7566_3903_4F76_26F4L, //  123
			0x43B1_2F82_B63E_2545L, 0x4451_C735_D92B_525DL, //  124
			0x6C4E_B26A_BD30_3BA2L, 0x3A1C_71EF_C1DE_EA2EL, //  125
			0x56A5_5B88_9759_C94EL, 0x61B0_5B26_34B2_54F2L, //  126
			0x4551_1606_DF7B_0772L, 0x1AF3_7C1E_908E_AA5BL, //  127
			0x6EE8_233E_325E_7250L, 0x2B1F_2CFD_B417_76F8L, //  128
			0x58B9_B5CB_5B7E_C1D9L, 0x6F4C_23FE_29AC_5F2DL, //  129
			0x46FA_F7D5_E2CB_CE47L, 0x72A3_4FFE_87BD_18F1L, //  130
			0x7191_8C89_6ADF_B073L, 0x0438_7FFD_A5FB_5B1BL, //  131
			0x5ADA_D6D4_557F_C05CL, 0x0360_6664_84C9_15AFL, //  132
			0x48AF_1243_7799_66B0L, 0x02B3_851D_3707_448CL, //  133
			0x744B_506B_F28F_0AB3L, 0x1DEC_082E_BE72_0746L, //  134
			0x5D09_0D23_2872_6EF5L, 0x64BC_D358_985B_3905L, //  135
			0x4A6D_A41C_205B_8BF7L, 0x6A30_A913_AD15_C738L, //  136
			0x7715_D360_33C5_ACBFL, 0x5D1A_A81F_7B56_0B8CL, //  137
			0x5F44_A919_C304_8A32L, 0x7DAE_ECE5_FC44_D609L, //  138
			0x4C36_EDAE_359D_3B5BL, 0x7E25_8A51_969D_7808L, //  139
			0x79F1_7C49_EF61_F893L, 0x16A2_76E8_F0FB_F33FL, //  140
			0x618D_FD07_F2B4_C6DCL, 0x121B_9253_F3FC_C299L, //  141
			0x4E0B_30D3_2890_9F16L, 0x41AF_A843_2997_0214L, //  142
			0x7CDE_B485_0DB4_31BDL, 0x4F7F_739E_A8F1_9CEDL, //  143
			0x63E5_5D37_3E29_C164L, 0x3F99_294B_BA5A_E3F1L, //  144
			0x4FEA_B0F8_FE87_CDE9L, 0x7FAD_BAA2_FB7B_E98DL, //  145
			0x7FDD_E7F4_CA72_E30FL, 0x7F7C_5DD1_925F_DC15L, //  146
			0x664B_1FF7_085B_E8D9L, 0x4C63_7E41_41E6_49ABL, //  147
			0x51D5_B32C_06AF_ED7AL, 0x704F_9834_34B8_3AEFL, //  148
			0x4177_C289_9EF3_2462L, 0x26A6_135C_F6F9_C8BFL, //  149
			0x68BF_9DA8_FE51_D3D0L, 0x3DD6_8561_8B29_4132L, //  150
			0x53CC_7E20_CB74_A973L, 0x4B12_044E_08ED_CDC2L, //  151
			0x4309_FE80_A2C3_BAC2L, 0x6F41_9D0B_3A57_D7CEL, //  152
			0x6B43_30CD_D139_2AD1L, 0x3202_94DE_C3BF_BFB0L, //  153
			0x55CF_5A3E_40FA_88A7L, 0x419B_AA4B_CFCC_995AL, //  154
			0x44A5_E1CB_672E_D3B9L, 0x1AE2_EEA3_0CA3_ADE1L, //  155
			0x6DD6_3612_3EB1_52C1L, 0x77D1_7DD1_ADD2_AFCFL, //  156
			0x57DE_91A8_3227_7567L, 0x7974_64A7_BE42_263FL, //  157
			0x464B_A7B9_C1B9_2AB9L, 0x4790_5086_31CE_84FFL, //  158
			0x7079_0C5C_6928_445CL, 0x0C1A_1A70_4FB0_D4CCL, //  159
			0x59FA_7049_EDB9_D049L, 0x567B_4859_D95A_43D6L, //  160
			0x47FB_8D07_F161_736EL, 0x11FC_39E1_7AAE_9CABL, //  161
			0x732C_14D9_8235_857DL, 0x032D_2968_C44A_9445L, //  162
			0x5C23_43E1_34F7_9DFDL, 0x4F57_5453_D03B_A9D1L, //  163
			0x49B5_CFE7_5D92_E4CAL, 0x72AC_4376_402F_BB0EL, //  164
			0x75EF_B30B_C8EB_07ABL, 0x0446_D256_CD19_2B49L, //  165
			0x5E59_5C09_6D88_D2EFL, 0x1D05_7512_3DAD_BC3AL, //  166
			0x4B7A_B007_8AD3_DBF2L, 0x4A6A_C40E_97BE_302FL, //  167
			0x78C4_4CD8_DE1F_C650L, 0x7711_39B0_F2C9_E6B1L, //  168
			0x609D_0A47_1819_6B73L, 0x78DA_948D_8F07_EBC1L, //  169
			0x4D4A_6E9F_467A_BC5CL, 0x60AE_DD3E_0C06_5634L, //  170
			0x7BAA_4A98_70C4_6094L, 0x344A_FB96_79A3_BD20L, //  171
			0x62EE_A213_8D69_E6DDL, 0x103B_FC78_614F_CA80L, //  172
			0x4F25_4E76_0ABB_1F17L, 0x2696_6393_810C_A200L, //  173
			0x7EA2_1723_445E_9825L, 0x2423_D285_9B47_6999L, //  174
			0x654E_78E9_037E_E01DL, 0x69B6_4204_7C39_2148L, //  175
			0x510B_93ED_9C65_8017L, 0x6E2B_6803_9694_1AA0L, //  176
			0x40D6_0FF1_49EA_CCDFL, 0x71BC_5336_1210_154DL, //  177
			0x67BC_E64E_DCAA_E166L, 0x1C60_8523_5019_BBAEL, //  178
			0x52FD_850B_E3BB_E784L, 0x7D1A_041C_4014_9625L, //  179
			0x4264_6A6F_E963_1F9DL, 0x4A7B_367D_0010_781DL, //  180
			0x6A3A_43E6_4238_3295L, 0x5D91_F0C8_001A_59C8L, //  181
			0x54FB_6985_01C6_8EDEL, 0x17A7_F3D3_3348_47D4L, //  182
			0x43FC_546A_67D2_0BE4L, 0x7953_2975_C2A0_3976L, //  183
			0x6CC6_ED77_0C83_463BL, 0x0EEB_7589_3766_C256L, //  184
			0x5705_8AC5_A39C_382FL, 0x2589_2AD4_2C52_3512L, //  185
			0x459E_089E_1C7C_F9BFL, 0x37A0_EF10_2374_F742L, //  186
			0x6F63_40FC_FA61_8F98L, 0x5901_7E80_38BB_2536L, //  187
			0x591C_33FD_951A_D946L, 0x7A67_9866_93C8_EA91L, //  188
			0x4749_C331_4415_7A9FL, 0x151F_AD1E_DCA0_BBA8L, //  189
			0x720F_9EB5_39BB_F765L, 0x0832_AE97_C767_92A5L, //  190
			0x5B3F_B22A_9496_5F84L, 0x068E_F213_05EC_7551L, //  191
			0x48FF_C1BB_AA11_E603L, 0x1ED8_C1A8_D189_F774L, //  192
			0x74CC_692C_434F_D66BL, 0x4AF4_690E_1C0F_F253L, //  193
			0x5D70_5423_690C_AB89L, 0x225D_20D8_1673_2843L, //  194
			0x4AC0_434F_873D_5607L, 0x3517_4D79_AB8F_5369L, //  195
			0x779A_054C_0B95_5672L, 0x21BE_E25C_45B2_1F0EL, //  196
			0x5FAE_6AA3_3C77_785BL, 0x3498_B516_9E28_18D8L, //  197
			0x4C8B_8882_96C5_F9E2L, 0x5D46_F745_4B53_4713L, //  198
			0x7A78_DA6A_8AD6_5C9DL, 0x7BA4_BED5_4552_0B52L, //  199
			0x61FA_4855_3BDE_B07EL, 0x2FB6_FF11_0441_A2A8L, //  200
			0x4E61_D377_6318_8D31L, 0x72F8_CC0D_9D01_4EEDL, //  201
			0x7D69_5258_9E8D_AEB6L, 0x1E5A_E015_C802_17E1L, //  202
			0x6454_41E0_7ED7_BEF8L, 0x1848_B344_A001_ACB4L, //  203
			0x5043_67E6_CBDF_CBF9L, 0x603A_2903_B334_8A2AL, //  204
			0x4035_ECB8_A319_6FFBL, 0x002E_8736_28F6_D4EEL, //  205
			0x66BC_ADF4_3828_B32BL, 0x19E4_0B89_DB24_87E3L, //  206
			0x5230_8B29_C686_F5BCL, 0x14B6_6FA1_7C1D_3983L, //  207
			0x41C0_6F54_9ED2_5E30L, 0x1091_F2E7_967D_C79CL, //  208
			0x6933_E554_3150_96B3L, 0x341C_B7D8_F0C9_3F5FL, //  209
			0x5429_8443_5AA6_DEF5L, 0x767D_5FE0_C0A0_FF80L, //  210
			0x4354_69CF_7BB8_B25EL, 0x2B97_7FE7_0080_CC66L, //  211
			0x6BBA_42E5_92C1_1D63L, 0x5F58_CCA4_CD9A_E0A3L, //  212
			0x562E_9BEA_DBCD_B11CL, 0x4C47_0A1D_7148_B3B6L, //  213
			0x44F2_1655_7CA4_8DB0L, 0x3D05_A1B1_276D_5C92L, //  214
			0x6E50_23BB_FAA0_E2B3L, 0x7B3C_35E8_3F15_60E9L, //  215
			0x5840_1C96_621A_4EF6L, 0x2F63_5E53_65AA_B3EDL, //  216
			0x4699_B078_4E7B_725EL, 0x591C_4B75_EAEE_F658L, //  217
			0x70F5_E726_E3F8_B6FDL, 0x74FA_1256_44B1_8A26L, //  218
			0x5A5E_5285_832D_5F31L, 0x43FB_41DE_9D5A_D4EBL, //  219
			0x484B_7537_9C24_4C27L, 0x4FFC_34B2_177B_DD89L, //  220
			0x73AB_EEBF_603A_1372L, 0x4CC6_BAB6_8BF9_6274L, //  221
			0x5C89_8BCC_4CFB_42C2L, 0x0A38_955E_D661_1B90L, //  222
			0x4A07_A309_D72F_689BL, 0x21C6_DDE5_784D_AFA7L, //  223
			0x7672_9E76_2518_A75EL, 0x693E_2FD5_8D49_190BL, //  224
			0x5EC2_185E_8413_B918L, 0x5431_BFDE_0AA0_E0D5L, //  225
			0x4BCE_79E5_3676_2DADL, 0x29C1_664B_3BB3_E711L, //  226
			0x794A_5CA1_F0BD_15E2L, 0x0F9B_D6DE_C5EC_A4E8L, //  227
			0x6108_4A1B_26FD_AB1BL, 0x2616_457F_04BD_50BAL, //  228
			0x4DA0_3B48_EBFE_227CL, 0x1E78_3798_D097_73C8L, //  229
			0x7C33_920E_4663_6A60L, 0x30C0_58F4_80F2_52D9L, //  230
			0x635C_74D8_384F_884DL, 0x0D66_AD90_6728_4247L, //  231
			0x4F7D_2A46_9372_D370L, 0x711E_F140_5286_9B6CL, //  232
			0x7F2E_AA0A_8584_8581L, 0x34FE_4ECD_50D7_5F14L, //  233
			0x65BE_EE6E_D136_D134L, 0x2A65_0BD7_73DF_7F43L, //  234
			0x5165_8B8B_DA92_40F6L, 0x551D_A312_C319_329CL, //  235
			0x411E_093C_AEDB_672BL, 0x5DB1_4F42_35AD_C217L, //  236
			0x6830_0EC7_7E2B_D845L, 0x7C4E_E536_BC49_368AL, //  237
			0x5359_A56C_64EF_E037L, 0x7D0B_EA92_303A_9208L, //  238
			0x42AE_1DF0_50BF_E693L, 0x173C_BBA8_2695_41A0L, //  239
			0x6AB0_2FE6_E799_70EBL, 0x3EC7_92A6_A422_029AL, //  240
			0x5559_BFEB_EC7A_C0BCL, 0x3239_421E_E9B4_CEE1L, //  241
			0x4447_CCBC_BD2F_0096L, 0x5B61_01B2_5490_A581L, //  242
			0x6D3F_ADFA_C84B_3424L, 0x2BCE_691D_541A_A268L, //  243
			0x5766_24C8_A03C_29B6L, 0x563E_BA7D_DCE2_1B87L, //  244
			0x45EB_50A0_8030_215EL, 0x7832_2ECB_171B_4939L, //  245
			0x6FDE_E767_3380_3564L, 0x59E9_E478_24F8_7527L, //  246
			0x597F_1F85_C2CC_F783L, 0x6187_E9F9_B72D_2A86L, //  247
			0x4798_E604_9BD7_2C69L, 0x346C_BB2E_2C24_2205L, //  248
			0x728E_3CD4_2C8B_7A42L, 0x20AD_F849_E039_D007L, //  249
			0x5BA4_FD76_8A09_2E9BL, 0x33BE_603B_19C7_D99FL, //  250
			0x4950_CAC5_3B3A_8BAFL, 0x42FE_B362_7B06_47B3L, //  251
			0x754E_113B_91F7_45E5L, 0x5197_856A_5E70_72B8L, //  252
			0x5DD8_0DC9_4192_9E51L, 0x27AC_6ABB_7EC0_5BC6L, //  253
			0x4B13_3E3A_9ADB_B1DAL, 0x52F0_5562_CBCD_1638L, //  254
			0x781E_C9F7_5E2C_4FC4L, 0x1E4D_556A_DFAE_89F3L, //  255
			0x6018_A192_B1BD_0C9CL, 0x7EA4_4455_7FBE_D4C3L, //  256
			0x4CE0_8142_27CA_707DL, 0x4BB6_9D11_32FF_109CL, //  257
			0x7B00_CED0_3FAA_4D95L, 0x5F8A_94E8_5198_1A93L, //  258
			0x6267_0BD9_CC88_3E11L, 0x32D5_43ED_0E13_4875L, //  259
			0x4EB8_D647_D6D3_64DAL, 0x5BDD_CFF0_D80F_6D2BL, //  260
			0x7DF4_8A0C_8AEB_D491L, 0x12FC_7FE7_C018_AEABL, //  261
			0x64C3_A1A3_A256_43A7L, 0x28C9_FFEC_99AD_5889L, //  262
			0x509C_814F_B511_CFB9L, 0x0707_FFF0_7AF1_13A1L, //  263
			0x407D_343F_C40E_3FC7L, 0x1F39_998D_2F27_42E7L, //  264
			0x672E_B9FF_A016_CC71L, 0x7EC2_8F48_4B72_04A4L, //  265
			0x528B_C7FF_B345_705BL, 0x189B_A5D3_6F8E_6A1DL, //  266
			0x4209_6CCC_8F6A_C048L, 0x7A16_1E42_BFA5_21B1L, //  267
			0x69A8_AE14_18AA_CD41L, 0x4356_96D1_32A1_CF81L, //  268
			0x5486_F1A9_AD55_7101L, 0x1C45_4574_2881_72CEL, //  269
			0x439F_27BA_F111_2734L, 0x169D_D129_BA01_28A5L, //  270
			0x6C31_D92B_1B4E_A520L, 0x242F_B50F_9001_DAA1L, //  271
			0x568E_4755_AF72_1DB3L, 0x368C_90D9_4001_7BB4L, //  272
			0x453E_9F77_BF8E_7E29L, 0x120A_0D7A_999A_C95DL, //  273
			0x6ECA_98BF_98E3_FD0EL, 0x5010_1590_F5C4_7561L, //  274
			0x58A2_13CC_7A4F_FDA5L, 0x2673_4473_F7D0_5DE8L, //  275
			0x46E8_0FD6_C83F_FE1DL, 0x6B8F_69F6_5FD9_E4B9L, //  276
			0x7173_4C8A_D9FF_FCFCL, 0x45B2_4323_CC8F_D45CL, //  277
			0x5AC2_A3A2_47FF_FD96L, 0x6AF5_0283_0A0C_A9E3L, //  278
			0x489B_B61B_6CCC_CADFL, 0x08C4_0202_6E70_87E9L, //  279
			0x742C_5692_47AE_1164L, 0x746C_D003_E3E7_3FDBL, //  280
			0x5CF0_4541_D2F1_A783L, 0x76BD_7336_4FEC_3315L, //  281
			0x4A59_D101_758E_1F9CL, 0x5EFD_F5C5_0CBC_F5ABL, //  282
			0x76F6_1B35_88E3_65C7L, 0x4B2F_EFA1_ADFB_22ABL, //  283
			0x5F2B_48F7_A0B5_EB06L, 0x08F3_261A_F195_B555L, //  284
			0x4C22_A0C6_1A2B_226BL, 0x20C2_84E2_5ADE_2AABL, //  285
			0x79D1_013C_F6AB_6A45L, 0x1AD0_D49D_5E30_4444L, //  286
			0x6174_00FD_9222_BB6AL, 0x48A7_107D_E4F3_69D0L, //  287
			0x4DF6_6731_41B5_62BBL, 0x53B8_D9FE_50C2_BB0DL, //  288
			0x7CBD_71E8_6922_3792L, 0x52C1_5CCA_1AD1_2B48L, //  289
			0x63CA_C186_BA81_C60EL, 0x7567_7D6E_7BDA_8906L, //  290
			0x4FD5_679E_FB9B_04D8L, 0x5DEC_6458_6315_3A6CL, //  291
			0x7FBB_D8FE_5F5E_6E27L, 0x497A_3A27_04EE_C3DFL, //  292
	};

	static final long[] POW10 = {
			1L,
			10L,
			100L,
			1_000L,
			10_000L,
			100_000L,
			1_000_000L,
			10_000_000L,
			100_000_000L,
			1_000_000_000L,
			10_000_000_000L,
			100_000_000_000L,
			1_000_000_000_000L,
			10_000_000_000_000L,
			100_000_000_000_000L,
			1_000_000_000_000_000L,
			10_000_000_000_000_000L,
			100_000_000_000_000_000L,
	};

	public static int writeFloatFastJson(float v, byte[] buf, int pos) {
		final int Q_MIN = -149;
		final int C_TINY = 8;
		final int C_MIN = 8388608;
		final int BQ_MASK = 255;
		final int T_MASK = 8388607;
		final long MASK_32 = 4294967295L;
		final int MASK_28 = 268435455;

		int bits = Float.floatToRawIntBits(v);
		int t = bits & T_MASK;
		int bq = (bits >>> 24 - 1) & BQ_MASK;
		if (bq < BQ_MASK) {
			int index = pos - 1;
			if (bits < 0)
				buf[++index] = '-';

			int q = 0;
			int c;
			int dk = 0;
			if (bq == 0 && t == 0) {
				if (bits != 0)
					buf[pos++] = '-';
				buf[pos] = '0';
				buf[pos + 1] = '.';
				buf[pos + 2] = '0';
				return pos + 3;
			}

			boolean _stat = false;
			int f = 0;
			int e = 0;
			if (bq != 0) {
				int mq = -Q_MIN + 1 - bq;
				c = C_MIN | t;
				if (0 < mq & mq < 24) {
					f = c >> mq;
					if (f << mq == c)
						_stat = true;
				}
				if (!_stat)
					q = -mq;
			} else {
				q = Q_MIN;
				if (t < C_TINY) {
					c = t * 10;
					dk = -1;
				} else
					c = t;
			}

			if (!_stat) {
				int out = c & 1;
				long cb = c << 2;
				long cbr = cb + 2;
				long cbl;
				int k;
				if (c != C_MIN | q == Q_MIN) {
					cbl = cb - 2;
					k = (int)(q * 661_971_961_083L >> 41);
				} else {
					cbl = cb - 1;
					k = (int)(q * 661_971_961_083L + (-274_743_187_321L) >> 41);
				}
				int h = q + (int)(-k * 913_124_641_741L >> 38) + 33;

				long g = G[k + 324 << 1] + 1;
				int vb;
				{
					long x1 = Math.multiplyHigh(g, cb << h);
					long vbp = x1 >>> 31;
					vb = (int)(vbp | (x1 & MASK_32) + MASK_32 >>> 32);
				}
				int vbl;
				{
					long x1 = Math.multiplyHigh(g, cbl << h);
					long vbp = x1 >>> 31;
					vbl = (int)(vbp | (x1 & MASK_32) + MASK_32 >>> 32);
				}

				int vbr;
				{
					long x1 = Math.multiplyHigh(g, cbr << h);
					long vbp = x1 >>> 31;
					vbr = (int)(vbp | (x1 & MASK_32) + MASK_32 >>> 32);
				}

				int s = vb >> 2;
				if (s >= 100) {
					int sp10 = (int)(s * 1_717_986_919L >>> 34) * 10;
					int tp10 = sp10 + 10;
					boolean upin = vbl + out <= sp10 << 2;
					boolean wpin = (tp10 << 2) + out <= vbr;
					if (upin != wpin) {
						_stat = true;
						f = upin ? sp10 : tp10;
						e = k;
					}
				}

				if (!_stat) {
					int t1 = s + 1;
					boolean uin = vbl + out <= s << 2;
					boolean win = (t1 << 2) + out <= vbr;
					if (uin != win)
						f = uin ? s : t1;
					else {
						int cmp = vb - (s + t1 << 1);
						f = cmp < 0 || cmp == 0 && (s & 1) == 0 ? s : t1;
					}
					e = k + dk;
				}
			}

			int len = (int)((Integer.SIZE - Integer.numberOfLeadingZeros(f)) * 661_971_961_083L >> 41);
			if (f >= POW10[len])
				len += 1;

			f *= (int)POW10[9 - len];
			e += len;

			int h1 = (int)(f * 1_441_151_881L >>> 57);
			int l = f - h1 * 100_000_000;

			if (0 < e && e <= 7) {
				buf[++index] = (byte)('0' + h1);
				int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
				int t1;
				int i = 1;
				for (; i < e; ++i) {
					t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}
				buf[++index] = '.';
				for (; i <= 8; ++i) {
					t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}

				while (buf[index] == '0')
					--index;
				if (buf[index] == '.')
					++index;
			} else if (-3 < e && e <= 0) {
				buf[1 + index] = '0';
				buf[2 + index] = '.';
				index += 2;
				for (; e < 0; ++e)
					buf[++index] = '0';
				buf[++index] = (byte)('0' + h1);

				// append8Digits(l);
				int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
				for (int i = 0; i < 8; ++i) {
					int t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}

				while (buf[index] == '0')
					--index;
				if (buf[index] == '.')
					++index;
			} else {
				buf[1 + index] = (byte)('0' + h1);
				buf[2 + index] = '.';
				index += 2;

				int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
				for (int i = 0; i < 8; ++i) {
					int t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}

				while (buf[index] == '0')
					--index;
				if (buf[index] == '.')
					++index;

				int e1 = e - 1;
				buf[++index] = 'E';
				if (e1 < 0) {
					buf[++index] = '-';
					e1 = -e1;
				}
				if (e1 < 10)
					buf[++index] = (byte)('0' + e1);
				else {
					int d = e1 * 103 >>> 10;
					buf[1 + index] = (byte)('0' + d);
					buf[2 + index] = (byte)('0' + (e1 - d * 10));
					index += 2;
				}
			}

			return index + 1;
		}

		if (t != 0) {
			buf[pos] = 'N';
			buf[pos + 1] = 'a';
			buf[pos + 2] = 'N';
			pos += 3;
		} else {
			if (bits <= 0)
				buf[pos++] = '-';
			putLong(buf, pos, 0x7974696e_69666e49L); // "Infinity"
			pos += 8;
		}
		return pos;
	}

	public static int writeDoubleFastJson(double v, byte[] buf, int pos) {
		final long C_TINY = 3;
		final long C_MIN = 4503599627370496L;
		final int BQ_MASK = 2047;
		final long T_MASK = 4503599627370495L;

		long bits = Double.doubleToRawLongBits(v);
		long t = bits & T_MASK;
		int bq = (int)(bits >>> 52) & BQ_MASK;
		if (bq < BQ_MASK) {
			int index = pos - 1;
			if (bits < 0)
				buf[++index] = '-';

			if (bq == 0 && t == 0) {
				if (bits != 0)
					buf[pos++] = '-';
				buf[pos] = '0';
				buf[pos + 1] = '.';
				buf[pos + 2] = '0';
				return pos + 3;
			}

			boolean _stat = false;
			long f = 0;
			int e = 0;
			int q = 0;
			long c;
			int dk = 0;
			if (bq != 0) {
				int mq = 1075 - bq;
				c = C_MIN | t;
				if (0 < mq & mq < 53) {
					f = c >> mq;
					if (f << mq == c)
						_stat = true;
				}
				if (!_stat)
					q = -mq;
			} else {
				q = -1074;
				if (t < C_TINY) {
					c = t * 10;
					dk = -1;
				} else
					c = t;
			}

			if (!_stat) {
				int out = (int)c & 1;
				long cb = c << 2;
				long cbr = cb + 2;
				long cbl;
				int k;
				if (c != C_MIN | q == -1074) {
					cbl = cb - 2;
					k = (int)(q * 661_971_961_083L >> 41);
				} else {
					cbl = cb - 1;
					k = (int)(q * 661_971_961_083L + (-274_743_187_321L) >> 41);
				}
				int h = q + (int)(-k * 913_124_641_741L >> 38) + 2;

				long g1 = G[k + 324 << 1];
				long g0 = G[k + 324 << 1 | 1];
				long vb;
				{
					long cp = cb << h;
					long z = ((g1 * cp) >>> 1) + Math.multiplyHigh(g0, cp);
					long vbp = Math.multiplyHigh(g1, cp) + (z >>> 63);
					vb = vbp | (z & 9223372036854775807L) + 9223372036854775807L >>> 63;
				}
				long vbl;
				{
					long cp = cbl << h;
					long z = ((g1 * cp) >>> 1) + Math.multiplyHigh(g0, cp);
					long vbp = Math.multiplyHigh(g1, cp) + (z >>> 63);
					vbl = vbp | (z & 9223372036854775807L) + 9223372036854775807L >>> 63;
				}
				long vbr;
				{
					long cp = cbr << h;
					long z = ((g1 * cp) >>> 1) + Math.multiplyHigh(g0, cp);
					long vbp = Math.multiplyHigh(g1, cp) + (z >>> 63);
					vbr = vbp | (z & 9223372036854775807L) + 9223372036854775807L >>> 63;
				}

				long s = vb >> 2;
				if (s >= 100) {
					long sp10 = Math.multiplyHigh(s, 115_292_150_460_684_698L << 4) * 10;
					long tp10 = sp10 + 10;
					boolean upin = vbl + out <= sp10 << 2;
					boolean wpin = (tp10 << 2) + out <= vbr;
					if (upin != wpin) {
						f = upin ? sp10 : tp10;
						e = k;
						_stat = true;
					}
				}

				if (!_stat) {
					long t1 = s + 1;
					boolean uin = vbl + out <= s << 2;
					boolean win = (t1 << 2) + out <= vbr;
					if (uin != win)
						f = uin ? s : t1;
					else {
						long cmp = vb - (s + t1 << 1);
						f = cmp < 0 || cmp == 0 && (s & 1) == 0 ? s : t1;
					}
					e = k + dk;
				}
			}

			int len = (int)((Long.SIZE - Long.numberOfLeadingZeros(f)) * 661_971_961_083L >> 41);
			if (f >= POW10[len])
				len += 1;

			final int H = 17;
			f *= POW10[H - len];
			e += len;

			long hm = Math.multiplyHigh(f, 193_428_131_138_340_668L) >>> 20;
			int l = (int)(f - hm * 100_000_000L);
			int h = (int)(hm * 1_441_151_881L >>> 57);
			int m = (int)(hm - h * 100_000_000);

			if (0 < e && e <= 7) {
				final int MASK_28 = 268435455;
				{
					buf[++index] = (byte)('0' + h);
					int y = (int)(Math.multiplyHigh((long)(m + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
					int t1;
					int i = 1;
					for (; i < e; ++i) {
						t1 = y * 10;
						buf[++index] = (byte)('0' + (t1 >>> 28));
						y = t1 & MASK_28;
					}
					buf[++index] = '.';
					for (; i <= 8; ++i) {
						t1 = y * 10;
						buf[++index] = (byte)('0' + (t1 >>> 28));
						y = t1 & MASK_28;
					}
				}

				if (l != 0) {
					int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
					for (int i = 0; i < 8; ++i) {
						int t1 = y * 10;
						buf[++index] = (byte)('0' + (t1 >>> 28));
						y = t1 & MASK_28;
					}
				}

				while (buf[index] == '0')
					--index;
				if (buf[index] == '.')
					++index;
				return index + 1;
			} else if (-3 < e && e <= 0) {
				buf[1 + index] = (byte)('0');
				buf[2 + index] = '.';
				index += 2;
				for (; e < 0; ++e)
					buf[++index] = (byte)('0');
				buf[++index] = (byte)('0' + h);

				final int MASK_28 = 268435455;
				{
					int y = (int)(Math.multiplyHigh((long)(m + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
					for (int i = 0; i < 8; ++i) {
						int t1 = y * 10;
						buf[++index] = (byte)('0' + (t1 >>> 28));
						y = t1 & MASK_28;
					}
				}

				if (l != 0) {
					int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
					for (int i = 0; i < 8; ++i) {
						int t1 = y * 10;
						buf[++index] = (byte)('0' + (t1 >>> 28));
						y = t1 & MASK_28;
					}
				}
				while (buf[index] == '0')
					--index;
				if (buf[index] == '.')
					++index;
				return index + 1;
			}

			buf[index + 1] = (byte)('0' + h);
			buf[index + 2] = '.';
			index += 2;

			final int MASK_28 = 268435455;
			{
				int y = (int)(Math.multiplyHigh((long)(m + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
				for (int i = 0; i < 8; ++i) {
					int t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}
			}

			if (l != 0) {
				int y = (int)(Math.multiplyHigh((long)(l + 1) << 28, 193_428_131_138_340_668L) >>> 20) - 1;
				for (int i = 0; i < 8; ++i) {
					int t1 = y * 10;
					buf[++index] = (byte)('0' + (t1 >>> 28));
					y = t1 & MASK_28;
				}
			}
			while (buf[index] == '0')
				--index;
			if (buf[index] == '.')
				++index;

			int e1 = e - 1;
			{
				buf[++index] = 'e';
				if (e1 < 0) {
					buf[++index] = '-';
					e1 = -e1;
				}
				if (e1 < 10)
					buf[++index] = (byte)('0' + e1);
				else {
					int d;
					if (e1 >= 100) {
						d = e1 * 1_311 >>> 17;
						buf[++index] = (byte)('0' + d);
						e1 -= d * 100;
					}
					d = e1 * 103 >>> 10;
					buf[index + 1] = (byte)('0' + d);
					buf[index + 2] = (byte)('0' + (e1 - d * 10));
					index += 2;
				}
			}
			return index + 1;
		}

		if (t != 0) {
			buf[pos] = 'N';
			buf[pos + 1] = 'a';
			buf[pos + 2] = 'N';
			pos += 3;
		} else {
			if (bits <= 0)
				buf[pos++] = '-';
			putLong(buf, pos, 0x7974696e_69666e49L); // "Infinity"
			pos += 8;
		}
		return pos;
	}

	public static void testFloatRange() {
		final byte[] buf = new byte[MAX_FLOAT_BYTES];
		float d = 0.01f;
		for (int j = 0; j < 5; j++, d *= 10) {
			int dd = Float.floatToRawIntBits(d);
			for (int i = 0; i < 1_000_000; i++) {
				final float f = Float.intBitsToFloat(dd + i);
				final int p = writeFloat(f, buf, 0);
				final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
				final float f2 = Float.parseFloat(s);
				if (f != f2)
					throw new AssertionError("testFloatRange[" + j + "," + i + "]: " + f + " != " + f2 + ", " + s);
			}
		}
		for (int e = 0; e < 512; e++) {
			final float f = Float.intBitsToFloat(e << 23);
			final int p = writeFloat(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2)
				throw new AssertionError("testFloatRange[e" + e + "]: " + f + " != " + f2 + ", " + s);
		}
		for (int e = 0; e < 512; e++) {
			final float f = Float.intBitsToFloat((e << 23) + 1);
			final int p = writeFloat(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2 && !(Float.isNaN(f) && Float.isNaN(f2)))
				throw new AssertionError("testFloatRange[e" + e + ":1]: " + f + " != " + f2 + ", " + s);
		}
		for (int i = 0; i < 1_000_000; i++) {
			final float f = Float.intBitsToFloat(i);
			final int p = writeFloat(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2)
				throw new AssertionError("testFloatRange[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		if (!"0".equals(new String(buf, 0, writeFloat(0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testFloatRange: 0");
		if (!"-0.0".equals(new String(buf, 0, writeFloat(-0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testFloatRange: -0.0");
		System.out.println("testFloatRange OK!");
	}

	public static void testFloatRandom() {
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		final byte[] buf = new byte[MAX_FLOAT_BYTES];
		for (int i = 0; i < 10_000_000; i++) {
			int v = r.nextInt();
			if ((v & 0x7f80_0000) == 0x7f80_0000) {
				v &= 0xffc0_0000; // Infinity/-Infinity/NaN
				if (v == 0xffc0_0000)
					v = 0x7fc0_0000;
			}
			final float f = Float.intBitsToFloat(v);
			final int p = writeFloat(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2 && !(Float.isNaN(f) && Float.isNaN(f2)))
				throw new AssertionError("testFloatRandom[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testFloatRandom OK!");
	}

	public static void testDoubleRange() {
		final byte[] buf = new byte[MAX_DOUBLE_BYTES];
		double d = 0.01;
		for (int j = 0; j < 5; j++, d *= 10) {
			long dd = Double.doubleToRawLongBits(d);
			for (int i = 0; i < 1_000_000; i++) {
				final double f = Double.longBitsToDouble(dd + i);
				final int p = writeDouble(f, buf, 0);
				final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
				final double f2 = Double.parseDouble(s);
				if (f != f2)
					throw new AssertionError("testDoubleRange[" + j + "," + i + "]: " + f + " != " + f2 + ", " + s);
			}
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits(e << 52);
			final int p = writeDouble(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleRange[e" + e + "]: " + f + " != " + f2 + ", " + s);
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits((e << 52) + 1);
			final int p = writeDouble(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			//noinspection ConstantValue
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleRange[e" + e + ":1]: " + f + " != " + f2 + ", " + s);
		}
		for (int i = 0; i < 1_000_000; i++) {
			final double f = Double.doubleToRawLongBits(i);
			final int p = writeDouble(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleRange[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		if (!"0".equals(new String(buf, 0, writeDouble(0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testDoubleRange: 0");
		if (!"-0.0".equals(new String(buf, 0, writeDouble(-0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testDoubleRange: -0.0");
		System.out.println("testDoubleRange OK!");
	}

	public static void testDoubleRandom() {
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		final byte[] buf = new byte[MAX_DOUBLE_BYTES];
		for (int i = 0; i < 10_000_000; i++) {
			long v = r.nextLong();
			if ((v & 0x7ff0_0000_0000_0000L) == 0x7ff0_0000_0000_0000L) {
				v &= 0xfff8_0000_0000_0000L; // Infinity/-Infinity/NaN
				if (v == 0xfff8_0000_0000_0000L)
					v = 0x7ff8_0000_0000_0000L;
			}
			final double f = Double.longBitsToDouble(v);
			final int p = writeDouble(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleRandom[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testDoubleRandom OK!");
	}

	public static void testFloatFastJsonRange() {
		final byte[] buf = new byte[MAX_FLOAT_BYTES];
		float d = 0.01f;
		for (int j = 0; j < 5; j++, d *= 10) {
			int dd = Float.floatToRawIntBits(d);
			for (int i = 0; i < 1_000_000; i++) {
				final float f = Float.intBitsToFloat(dd + i);
				final int p = writeFloatFastJson(f, buf, 0);
				final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
				final float f2 = Float.parseFloat(s);
				if (f != f2)
					throw new AssertionError("testFloatFastJsonRange[" + j + "," + i + "]: " + f + " != " + f2 + ", " + s);
			}
		}
		for (int e = 0; e < 512; e++) {
			final float f = Float.intBitsToFloat(e << 23);
			final int p = writeFloatFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2)
				throw new AssertionError("testFloatFastJsonRange[e" + e + "]: " + f + " != " + f2 + ", " + s);
		}
		for (int e = 0; e < 512; e++) {
			final float f = Float.intBitsToFloat((e << 23) + 1);
			final int p = writeFloatFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2 && !(Float.isNaN(f) && Float.isNaN(f2)))
				throw new AssertionError("testFloatFastJsonRange[e" + e + ":1]: " + f + " != " + f2 + ", " + s);
		}
		for (int i = 0; i < 1_000_000; i++) {
			final float f = Float.intBitsToFloat(i);
			final int p = writeFloatFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2)
				throw new AssertionError("testFloatFastJsonRange[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		if (!"0.0".equals(new String(buf, 0, writeFloatFastJson(0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testFloatFastJsonRange: 0.0");
		if (!"-0.0".equals(new String(buf, 0, writeFloatFastJson(-0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testFloatFastJsonRange: -0.0");
		System.out.println("testFloatFastJsonRange OK!");
	}

	public static void testFloatFastJsonRandom() {
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		final byte[] buf = new byte[MAX_FLOAT_BYTES];
		for (int i = 0; i < 10_000_000; i++) {
			int v = r.nextInt();
			if ((v & 0x7f80_0000) == 0x7f80_0000) {
				v &= 0xffc0_0000; // Infinity/-Infinity/NaN
				if (v == 0xffc0_0000)
					v = 0x7fc0_0000;
			}
			final float f = Float.intBitsToFloat(v);
			final int p = writeFloatFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final float f2 = Float.parseFloat(s);
			if (f != f2 && !(Float.isNaN(f) && Float.isNaN(f2)))
				throw new AssertionError("testFloatFastJsonRandom[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testFloatFastJsonRandom OK!");
	}

	public static void testDoubleFastJsonRange() {
		final byte[] buf = new byte[MAX_DOUBLE_BYTES];
		double d = 0.01;
		for (int j = 0; j < 5; j++, d *= 10) {
			long dd = Double.doubleToRawLongBits(d);
			for (int i = 0; i < 1_000_000; i++) {
				final double f = Double.longBitsToDouble(dd + i);
				final int p = writeDoubleFastJson(f, buf, 0);
				final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
				final double f2 = Double.parseDouble(s);
				if (f != f2)
					throw new AssertionError("testDoubleFastJsonRange[" + j + "," + i + "]: " + f + " != " + f2 + ", " + s);
			}
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits(e << 52);
			final int p = writeDoubleFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleFastJsonRange[e" + e + "]: " + f + " != " + f2 + ", " + s);
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits((e << 52) + 1);
			final int p = writeDoubleFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			//noinspection ConstantValue
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleFastJsonRange[e" + e + ":1]: " + f + " != " + f2 + ", " + s);
		}
		for (int i = 0; i < 1_000_000; i++) {
			final double f = Double.doubleToRawLongBits(i);
			final int p = writeDoubleFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleFastJsonRange[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		if (!"0.0".equals(new String(buf, 0, writeDoubleFastJson(0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testDoubleFastJsonRange: 0.0");
		if (!"-0.0".equals(new String(buf, 0, writeDoubleFastJson(-0.0f, buf, 0), StandardCharsets.ISO_8859_1)))
			throw new AssertionError("testDoubleFastJsonRange: -0.0");
		System.out.println("testDoubleFastJsonRange OK!");
	}

	public static void testDoubleFastJsonRandom() {
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		final byte[] buf = new byte[MAX_DOUBLE_BYTES];
		for (int i = 0; i < 10_000_000; i++) {
			long v = r.nextLong();
			if ((v & 0x7ff0_0000_0000_0000L) == 0x7ff0_0000_0000_0000L) {
				v &= 0xfff8_0000_0000_0000L; // Infinity/-Infinity/NaN
				if (v == 0xfff8_0000_0000_0000L)
					v = 0x7ff8_0000_0000_0000L;
			}
			final double f = Double.longBitsToDouble(v);
			final int p = writeDoubleFastJson(f, buf, 0);
			final String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
			final double f2 = Double.parseDouble(s);
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleFastJsonRandom[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testDoubleFastJsonRandom OK!");
	}

	private static final float[] testFloats = {
			3.1234567f, 31234567f, 0.31234567f, 312.34567f, 3.1234567e7f, 3.1234567E-7f, 0, 1.0f
	};

	private static final double[] testDoubles = {
			3.1234567, 31234567, 0.31234567, 312.34567, 3.1234567e7, 3.1234567E-7, 0, 1.0
	};

	public static void benchmark() {
		long n, t;
		byte[] buf;

		JsonWriter jw = new JsonWriter();
		n = 0L;
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
//				n += Double.toString(tests[j]).length();
				jw.clear().write(testDoubles[j]);
				n += jw.size();
//				if (i == 0)
//					System.out.println(jw);
			}
		}
		System.out.format("    JasonWriter: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 660000000

		n = 0L;
		buf = new byte[MAX_FLOAT_BYTES];
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
				int m = writeFloat(testFloats[j], buf, 0);
				n += m;
//				if (i == 0)
//					System.out.println(new String(buf, 0, m));
			}
		}
		System.out.format(" DragonBoxFloat: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 680000000

		n = 0L;
		buf = new byte[MAX_DOUBLE_BYTES];
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
				int m = writeDouble(testDoubles[j], buf, 0);
				n += m;
//				if (i == 0)
//					System.out.println(new String(buf, 0, m));
			}
		}
		System.out.format("DragonBoxDouble: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 680000000

		n = 0L;
		buf = new byte[MAX_FLOAT_BYTES];
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
				int m = writeFloatFastJson(testFloats[j], buf, 0);
				n += m;
//				if (i == 0)
//					System.out.println(new String(buf, 0, m));
			}
		}
		System.out.format("  FastJsonFloat: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 680000000

		n = 0L;
		buf = new byte[MAX_DOUBLE_BYTES];
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
				int m = writeDoubleFastJson(testDoubles[j], buf, 0);
				n += m;
//				if (i == 0)
//					System.out.println(new String(buf, 0, m));
			}
		}
		System.out.format(" FastJsonDouble: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 680000000
	}

	private static final byte[] LEN_TABLE = { // [65] index=numberOfLeadingZeros
			19, 19, 19, 19, 18, 18, 18, 17, 17, 17, 16, 16, 16, 16, 15, 15, // 0~15
			15, 14, 14, 14, 13, 13, 13, 13, 12, 12, 12, 11, 11, 11, 10, 10, // 16~31
			10, 10, 9, 9, 9, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 5, // 32~47
			5, 5, 4, 4, 4, 4, 3, 3, 3, 2, 2, 2, 1, 1, 1, 1, 1 // 48~64
	};

	private static final long[] POW10M1 = { // [20]
			0, 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, 9999999999L, 99999999999L, 999999999999L,
			9999999999999L, 99999999999999L, 999999999999999L, 9999999999999999L, 99999999999999999L,
			999999999999999999L, 0x8AC7_2304_89E7_FFFFL,
	};

	public static int unsignedDecimalLen(long u) { // return:1~20
		int n = LEN_TABLE[Long.numberOfLeadingZeros(u)];
		return n + (int)((POW10M1[n] - u) >>> 63);
	}

	public static void testUnsignedDecimalLen() {
		System.out.println(unsignedDecimalLen(0));
		System.out.println(unsignedDecimalLen(1));
		System.out.println(unsignedDecimalLen(2));
		System.out.println(unsignedDecimalLen(9999));
		System.out.println(unsignedDecimalLen(10000));
		System.out.println(unsignedDecimalLen(10001));
		System.out.println(unsignedDecimalLen(-1));
		int a = 0;
		for (int i = 0; i < 1_000_000; i++)
			a += unsignedDecimalLen(i);
		System.out.println(a);
	}

	public static void main(String[] args) {
//		byte[] buf = new byte[MAX_FLOAT_BYTES];
//		float f = 1.4742742E-21f;
//		int p = writeFloat(f, buf, 0);
//		String s = new String(buf, 0, p, StandardCharsets.ISO_8859_1);
//		System.out.println(s);
//		System.out.println(Float.parseFloat(s));
//		System.out.println(f);

//		byte[] buf = new byte[MAX_DOUBLE_BYTES];
//		int p = writeDouble(1.0, buf, 0);
//		System.out.println(new String(buf, 0, p, StandardCharsets.ISO_8859_1));

//		testUnsignedDecimalLen();

		testFloatRange();
		testFloatRandom();
		testDoubleRange();
		testDoubleRandom();
		testFloatFastJsonRange();
		testFloatFastJsonRandom();
		testDoubleFastJsonRange();
		testDoubleFastJsonRandom();
		for (int i = 0; i < 5; i++)
			benchmark();
	}
}
