package hudson.plugins.bruceschneier;

import java.util.Random;

public class FactGenerator {

	// source:
	// http://www.schneierfacts.com/facts/top
	private static final String[] FACTS = {
        "Bruce Schneier knows Alice and Bob's shared secret.",
        "Most people use passwords. Some people use passphrases. Bruce Schneier uses an epic passpoem, detailing the life and works of seven mythical Norse heroes.",
        "Bruce Schneier's secure handshake is so strong, you won't be able to exchange keys with anyone else for days.",
        "Bruce Schneier once decrypted a box of AlphaBits.",
        "Vs lbh nfxrq Oehpr Fpuarvre gb qrpelcg guvf, ur'q pehfu lbhe fxhyy jvgu uvf ynhtu.",
        "Bruce Schneier writes his books and essays by generating random alphanumeric text of an appropriate length and then decrypting it.",
        "If we built a Dyson sphere around Bruce Schneier and captured all of his energy for 2 months, without any loss, we could power an ideal computer running at 3.2 degrees K to count up to 2^256. This strongly implies that not only can Bruce Schneier brute-force attack 256-bit keys, but that he is built of something other than matter and occupies something other than space.",
        "Bruce Schneier knows the state of schroedinger's cat",
        "When Bruce Schneier observes a quantum particle, it remains in the same state until he has finished observing it.",
        "Though a superhero, Bruce Schneier disdanes the use of a mask or secret identity as 'security through obscurity'.",
	"When Bruce Schneier calculates the square root of a negative number the result is real."
	};

	private static final Random RANDOM = new Random();;

	public String random() {
		return FACTS[RANDOM.nextInt(FACTS.length)];
	}
}
