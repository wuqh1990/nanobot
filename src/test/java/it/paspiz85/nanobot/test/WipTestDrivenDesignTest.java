package it.paspiz85.nanobot.test;

import it.paspiz85.nanobot.test.FeaturesConfig.Tag;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "classpath:features" }, glue = { FeaturesConfig.GLUE }, strict = true, plugin = { "pretty" }, tags = {
        Tag.TEST, Tag.TDD, Tag.NO_DEPRECATED })
public class WipTestDrivenDesignTest {
}
