package jason;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

//ref: https://github.com/jk-jeon/dragonbox/tree/master/subproject/simple
public class DragonBox {
	private static final long[] DOUBLE_TABLE = new long[]{ // uint128(high,low)[619]
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

	public static final int MAX_DOUBLE_BYTES = 24; // 4 + (DECIMAL_SIGNIFICAND_DIGITS=17) + (DECIMAL_EXPONENT_DIGITS=3);

	public static int writeDouble(final double f, final byte[] buf, int pos) {
		final int TOTAL_BITS = 64;
		final int SIGNIFICAND_BITS = 52;
		final int EXPONENT_BITS = 11;
		final int MIN_EXPONENT = -1022; // MAX_EXPONENT = 1023
		final int EXPONENT_BIAS = -1023;
		final int MIN_K = -292;
		final int KAPPA = 2;
		final int DIVIDE_MAGIC_NUMBER = 656; // KAPPA == 1 ? 6554 : 656;
		final int BIG_DIVISOR = 1000;
		final long u = Double.doubleToRawLongBits(f);
		final long u2 = u << 1;
		if (u2 == 0) {
			if (u < 0)
				buf[pos++] = '-';
			buf[pos++] = '0';
			return pos;
		}
		long num = u & ((1L << SIGNIFICAND_BITS) - 1);
		int exp = (int)(u2 >>> (SIGNIFICAND_BITS + 1));
		if (exp == (1 << EXPONENT_BITS) - 1) {
			if (num == 0) {
				if (u < 0)
					buf[pos++] = '-';
				buf[pos++] = 'I';
				buf[pos++] = 'n';
				buf[pos++] = 'f';
				buf[pos++] = 'i';
				buf[pos++] = 'n';
				buf[pos++] = 'i';
				buf[pos++] = 't';
				buf[pos++] = 'y';
			} else {
				buf[pos++] = 'N';
				buf[pos++] = 'a';
				buf[pos++] = 'N';
			}
			return pos;
		}
		if (u < 0)
			buf[pos++] = '-';
		do {
			final long num2;
			if (exp == 0) {
				exp = MIN_EXPONENT - SIGNIFICAND_BITS;
				num2 = num * 2;
			} else if (num != 0) {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				num2 = num * 2 + (1L << (SIGNIFICAND_BITS + 1));
			} else {
				exp += EXPONENT_BIAS - SIGNIFICAND_BITS;
				final int minusK = (exp * 631305 - 261663) >> 21;
				final int beta = exp + ((-minusK * 1741647) >> 19);
				final long cacheHigh = DOUBLE_TABLE[(-minusK - MIN_K) * 2];
				final long xi = ((cacheHigh - (cacheHigh >>> (SIGNIFICAND_BITS + 2)))
						>>> (TOTAL_BITS - SIGNIFICAND_BITS - 1 - beta)) + 1;
				final long zi = (cacheHigh + (cacheHigh >>> (SIGNIFICAND_BITS + 1)))
						>>> (TOTAL_BITS - SIGNIFICAND_BITS - 1 - beta);
				num = Math.unsignedMultiplyHigh(zi, 1844674407370955162L);
				if (Long.compareUnsigned(num * 10, xi) >= 0) {
					long r = Long.rotateRight(num * 0x67_074B_22E9_0E21L, 8);
					int s = Long.compareUnsigned(r, 184467440738L) >>> 31;
					if (s != 0)
						num = r;
					r = Long.rotateRight(num * 0x288_CE70_3AFB_7E91L, 4);
					int b = Long.compareUnsigned(r, 1844674407370956L) >>> 31;
					s = s * 2 + b;
					if (b != 0)
						num = r;
					r = Long.rotateRight(num * 0x8F5C_28F5_C28F_5C29L, 2);
					b = Long.compareUnsigned(r, 184467440737095517L) >>> 31;
					s = s * 2 + b;
					if (b != 0)
						num = r;
					r = Long.rotateRight(num * 0xCCCC_CCCC_CCCC_CCCDL, 1);
					b = Long.compareUnsigned(r, 1844674407370955162L) >>> 31;
					s = s * 2 + b;
					if (b != 0)
						num = r;
					exp = minusK + s + 1;
				} else {
					num = (((cacheHigh >>> (TOTAL_BITS - SIGNIFICAND_BITS - 2 - beta)) + 1) >>> 1)
							+ Long.compareUnsigned(num, xi) >>> 31;
					exp = minusK;
				}
				break;
			}
			final int minusK = ((exp * 315653) >> 20) - KAPPA;
			final int beta = exp + ((-minusK * 1741647) >> 19);
			final int p = (-minusK - MIN_K) * 2;
			final long cacheHigh = DOUBLE_TABLE[p];
			final long cacheLow = DOUBLE_TABLE[p + 1];
			final long deltai = cacheHigh >>> (TOTAL_BITS - 1 - beta);
			final long uu = (num2 | 1) << beta;
			final long rLow = uu * cacheHigh;
			final long zResult = Math.unsignedMultiplyHigh(uu, cacheHigh)
					+ (Long.compareUnsigned(Math.unsignedMultiplyHigh(uu, cacheLow) + rLow, rLow) >>> 31);
			num = Math.unsignedMultiplyHigh(zResult, 4722366482869645214L) >>> 8;
			long r = zResult - num * BIG_DIVISOR;
			final long num3;
			if (Long.compareUnsigned(r, deltai) > 0 || r == deltai && ((((num3 = num2 - 1) * cacheHigh
					+ Math.unsignedMultiplyHigh(num3, cacheLow)) >>> (64 - beta)) & 1) == 0) {
				num = num * 10 + ((r * DIVIDE_MAGIC_NUMBER) >>> 16);
				exp = minusK + KAPPA;
			} else {
				r = Long.rotateRight(num * 0x67_074B_22E9_0E21L, 8);
				int s = Long.compareUnsigned(r, 184467440738L) >>> 31;
				if (s != 0)
					num = r;
				r = Long.rotateRight(num * 0x288_CE70_3AFB_7E91L, 4);
				int b = Long.compareUnsigned(r, 1844674407370956L) >>> 31;
				s = s * 2 + b;
				if (b != 0)
					num = r;
				r = Long.rotateRight(num * 0x8F5C_28F5_C28F_5C29L, 2);
				b = Long.compareUnsigned(r, 184467440737095517L) >>> 31;
				s = s * 2 + b;
				if (b != 0)
					num = r;
				r = Long.rotateRight(num * 0xCCCC_CCCC_CCCC_CCCDL, 1);
				b = Long.compareUnsigned(r, 1844674407370955162L) >>> 31;
				s = s * 2 + b;
				if (b != 0)
					num = r;
				exp = minusK + s + KAPPA + 1;
			}
		} while (false);
		if (num < 10)
			buf[pos++] = (byte)('0' + num);
		else {
			int begin = pos;
			do {
				buf[pos++] = (byte)('0' + num % 10);
				num /= 10;
				exp++;
			} while (num >= 10);
			buf[pos++] = '.';
			buf[pos++] = (byte)('0' + num);
			for (int e = pos; begin < --e; ) {
				byte b = buf[begin];
				buf[begin++] = buf[e];
				buf[e] = b;
			}
		}
		if (exp != 0) {
			buf[pos++] = 'e';
			if (exp < 0) {
				exp = -exp;
				buf[pos++] = '-';
			}
			int begin = pos;
			do {
				buf[pos++] = (byte)('0' + exp % 10);
				exp /= 10;
			} while (exp != 0);
			for (int e = pos; begin < --e; ) {
				byte b = buf[begin];
				buf[begin++] = buf[e];
				buf[e] = b;
			}
		}
		return pos;
	}

	public static void testRange() {
		// final var jr = new JsonReader();
		final var buf = new byte[MAX_DOUBLE_BYTES];
		long d = Double.doubleToRawLongBits(0.01);
		for (int j = 0; j < 5; j++, d *= 10) {
			for (int i = 0; i < 1_000_000; i++) {
				final var f = Double.longBitsToDouble(d + i);
				final var p = writeDouble(f, buf, 0);
				// buf[p] = 0;
				final var f2 = Double.parseDouble(new String(buf, 0, p, StandardCharsets.ISO_8859_1)); // jr.buf(buf).parseDouble();
				if (f != f2) {
					throw new AssertionError("testRange[" + i + "]: " + f + " != " + f2
							+ ", " + new String(buf, 0, p, StandardCharsets.ISO_8859_1));
				}
			}
		}
		System.out.println("testRange OK!");
	}

	public static void testRandom() {
		final var r = ThreadLocalRandom.current();
		// final var jr = new JsonReader();
		final var buf = new byte[MAX_DOUBLE_BYTES];
		for (int i = 0; i < 1_000_000; i++) {
			final var f = r.nextDouble();
			final var p = writeDouble(f, buf, 0);
			// buf[p] = 0;
			final var f2 = Double.parseDouble(new String(buf, 0, p, StandardCharsets.ISO_8859_1)); // jr.buf(buf).parseDouble();
			if (f != f2) {
				throw new AssertionError("testRandom[" + i + "]: " + f + " != " + f2
						+ ", " + new String(buf, 0, p, StandardCharsets.ISO_8859_1));
			}
		}
		System.out.println("testRandom OK!");
	}

	public static void testParserBug() {
		for (long i = 9639801287119911L; i <= 9639801287119919L; i++)
			System.out.println(i + " => " + (double)i);
		System.out.println(0.9639801287119912);
		System.out.println(0.9639801287119913);
		System.out.println(0.9639801287119914);

		System.out.println(Double.longBitsToDouble(4606857980642196189L)); // 0.9639801287119912
		System.out.println(Double.longBitsToDouble(4606857980642196190L)); // 0.9639801287119913
		System.out.println(Double.longBitsToDouble(4606857980642196191L)); // 0.9639801287119915

		double d = 0.9639801287119913;
		System.out.println(d);
		//System.out.println((long)d);
		System.out.format("%.20f\n", d);

		var jw = JsonWriter.local();
		jw.write(d);
		System.out.println(jw);

		var buf = new byte[MAX_DOUBLE_BYTES];
		var pos = writeDouble(0.9639801287119913, buf, 0);
		System.out.println(new String(buf, 0, pos, StandardCharsets.ISO_8859_1));

		System.out.println(Double.doubleToRawLongBits(0.9639801287119913));
		System.out.println(Double.doubleToRawLongBits(JsonReader.local().buf("9.639801287119913e-1 ").parseDouble()));

		System.out.println(JsonReader.local().buf("0.9639801287119913 ").parseDouble());
	}

	private static final double[] testNums = {3.1234567, 31234567, 0.31234567, 312.34567, 3.1234567e7, 3.1234567E-7, 0,
			1.0};

	public static void benchmark() {
		var jw = new JsonWriter();
		var n = 0L;
		var t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
//				n += Double.toString(tests[j]).length();
				jw.clear().write(testNums[j]);
				n += jw.size();
//				System.out.println(new String(jw.buf, 0, jw.pos));
			}
		}
		System.out.format("   JasonWriter: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 660000000

		n = 0L;
		var buf = new byte[MAX_DOUBLE_BYTES];
		t = System.nanoTime();
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++)
				n += writeDouble(testNums[j], buf, 0);
		}
		System.out.format("     DragonBox: %d (%d ms)%n", n, (System.nanoTime() - t) / 1_000_000); // 660000000
	}

	private static final byte[] LEN_TABLE = { // [64]
			19, 19, 19, 19, 18, 18, 18, 17, 17, 17, 16, 16, 16, 16, 15, 15,
			15, 14, 14, 14, 13, 13, 13, 13, 12, 12, 12, 11, 11, 11, 10, 10,
			10, 10, 9, 9, 9, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 5,
			5, 5, 4, 4, 4, 4, 3, 3, 3, 2, 2, 2, 1, 1, 1, 1,
	};

	@SuppressWarnings("NumericOverflow")
	private static final long[] CMP_TABLE = { // [20]
			1L,
			10L,
			100L,
			1000L,
			10000L,
			100000L,
			1000000L,
			10000000L,
			100000000L,
			1000000000L,
			10000000000L,
			100000000000L,
			1000000000000L,
			10000000000000L,
			100000000000000L,
			1000000000000000L,
			10000000000000000L,
			100000000000000000L,
			1000000000000000000L,
			1000000000000000000L * 10,
	};

	public static int decimalLen(long u) {
		int n = LEN_TABLE[Long.numberOfLeadingZeros(u)];
		return n;
	}

	public static void main(String[] args) {
//		var buf = new byte[MAX_LEN];
//		int p = toChars(1.0, buf, 0);
//		System.out.println(new String(buf, 0, p, StandardCharsets.ISO_8859_1));

		testRange();
		testRandom();
		for (int i = 0; i < 5; i++)
			benchmark();
	}
}
