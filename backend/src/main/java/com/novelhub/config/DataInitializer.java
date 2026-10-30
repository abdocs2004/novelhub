package com.novelhub.config;

import com.novelhub.entity.Chapter;
import com.novelhub.entity.Novel;
import com.novelhub.entity.Role;
import com.novelhub.entity.User;
import com.novelhub.repository.NovelRepository;
import com.novelhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Runs once after the application starts.
 *  - creates the first ADMIN (public registration can only create normal USERs)
 *  - inserts a small demo novel so the UI is not empty
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean seedDemoData;

    public DataInitializer(UserRepository userRepository,
                           NovelRepository novelRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.name}") String adminName,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword,
                           @Value("${app.seed-demo-data}") boolean seedDemoData) {
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
        this.seedDemoData = seedDemoData;
    }

    @Override
    public void run(String... args) {
        createAdminIfMissing();
        if (seedDemoData && novelRepository.count() == 0) {
            createDemoNovel();
        }
    }

    private void createAdminIfMissing() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.warn("Created ADMIN user '{}'. Change ADMIN_EMAIL / ADMIN_PASSWORD before deploying anywhere real!", adminEmail);
    }

    private void createDemoNovel() {
        Novel novel = new Novel();
        novel.setTitle("The Lighthouse Ledger");
        novel.setAuthor("Demo Author");
        novel.setDescription("A lighthouse keeper finds a ledger that lists ships that have not been built yet. "
                + "A short demo novel with one free chapter and two paid chapters.");

        novel.addChapter(chapter(1, "The Ledger in the Lamp Room", "0.00", """
                The stairs of the lighthouse had ninety-nine steps, and Mara had counted them every morning for eleven years.

                On the hundredth morning of the storm season she found a hundredth thing instead: a leather ledger on the \
                floor of the lamp room, dry although the window above it had been open all night.

                The first page was ruled in careful columns. Date, vessel, cargo, fate. Every line was already filled in, \
                and every date was in the future."""));

        novel.addChapter(chapter(2, "Ships Without Harbours", "2.50", """
                By noon Mara had read the ledger twice. The vessels had names she had never heard, like the Quiet Margin \
                and the Second Apology, and the column marked "fate" said only one of two words: Kept, or Returned.

                She put the ledger in the drawer beneath the logbook and told herself that ink can lie as easily as a person.

                At dusk a ship she did not recognise passed the rocks with all its lamps unlit."""));

        novel.addChapter(chapter(3, "What the Sea Returns", "3.00", """
                The Quiet Margin never reached the harbour. Mara watched it dissolve into the fog and then, an hour later, \
                she watched it come back out of the same fog, older by about a hundred years.

                She opened the drawer. Under the date, in handwriting that looked a great deal like her own, \
                the ledger had written: Returned.

                She decided that the lamp would stay lit that night, ledger or no ledger."""));

        novelRepository.save(novel); // cascade saves the 3 chapters too
        log.info("Inserted demo novel '{}'", novel.getTitle());
    }

    private Chapter chapter(int number, String title, String price, String content) {
        Chapter chapter = new Chapter();
        chapter.setChapterNumber(number);
        chapter.setTitle(title);
        chapter.setPrice(new BigDecimal(price));
        chapter.setContent(content);
        return chapter;
    }
}
