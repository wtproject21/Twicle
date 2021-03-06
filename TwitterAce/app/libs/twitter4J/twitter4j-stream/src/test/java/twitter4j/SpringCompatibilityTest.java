/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
class SpringCompatibilityTest {
    XmlBeanFactory beanFactory;

    @BeforeEach
    protected void setUp() throws Exception {
        writeFile("libs/twitter4j.properties", "user=one"
                + "\n" + "password=pasword-one");
        Resource res = new ClassPathResource("spring-beans.xml");
        beanFactory = new XmlBeanFactory(res);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        deleteFile("libs/twitter4j.properties");
    }

    @Test
    void testFactoryInstantiation() throws Exception {
        TwitterFactory twitterFactory = (TwitterFactory) beanFactory.getBean("twitterFactory");
        Twitter twitter = twitterFactory.getInstance();
        assertTrue(twitter instanceof Twitter);

        AsyncTwitterFactory asyncTwitterFactory = (AsyncTwitterFactory) beanFactory.getBean("asyncTwitterFactory");
        AsyncTwitter asyncTwitter = asyncTwitterFactory.getInstance();
        assertTrue(asyncTwitter instanceof AsyncTwitter);

        TwitterStreamFactory twitterStreamFactory = (TwitterStreamFactory) beanFactory.getBean("twitterStreamFactory");
        TwitterStream twitterStream = twitterStreamFactory.getInstance();
        assertTrue(twitterStream instanceof TwitterStream);
    }

    @Test
    void testTwitterInstantiation() throws Exception {
        Twitter twitter = (Twitter) beanFactory.getBean("twitter");
        assertTrue(twitter instanceof Twitter);

        AsyncTwitter asyncTwitter = (AsyncTwitter) beanFactory.getBean("asyncTwitter");
        assertTrue(asyncTwitter instanceof AsyncTwitter);

        TwitterStream twitterStream = (TwitterStream) beanFactory.getBean("twitterStream");
        assertTrue(twitterStream instanceof TwitterStream);
    }

    private void writeFile(String path, String content) throws IOException {
        File file = new File(path);
        file.delete();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    private void deleteFile(String path) throws IOException {
        File file = new File(path);
        file.delete();
    }
}
