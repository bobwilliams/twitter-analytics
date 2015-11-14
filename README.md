# Twitter Analytics

A Clojure app for generating stats and NLP analytics from Twitter.

## Dependencies

* [Twitter API](https://github.com/adamwynne/twitter-api)
* [Clojure OpenNLP](https://github.com/dakrone/clojure-opennlp)
* [Cheshire](https://github.com/dakrone/cheshire)
* [Leiningen](http://leiningen.org/)
* [OpenNLP Models](http://opennlp.sourceforge.net/models-1.5)

## Usage

### configuration
You will need to [create a new app](https://apps.twitter.com/) for Twitter.
Once done, head over to your Keys and Access Tokens page to grab most of the below.

```json
{
    "consumer-key": "",           // your twitter consumer key (api key)
    "consumer-secret": "",        // your twitter consumer secrect key (api secret)
    "access-token": "",           // your access token
    "access-token-secret": "",    // your access token secret
    "num-tweets-to-fetch": 200,   // max amount of tweets to fetch, note: twitter rate limits this to 200
    "stats-cap": 10,              // max or top x results to report on
    "screen-name": ""             //twitter handle minus the '@'' symbol
}
```

### running the application 

```sh
$ cd <project folder>
$ lein run <optional config>
```
or from the REPL

```sh
$ cd <project folder>
$ lein repl
$ (-main)
```

## License

This project is Copyright (c) 2015 [Bob Williams](https://github.com/bobwilliams/) and open sourced under [GNU GPL v3.0](LICENSE.txt).
