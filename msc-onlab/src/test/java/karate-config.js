function fn() {
    karate.configure('proxy', 'http://localhost:8899');

    var baseUrl = 'https://www.google.com';

    return { baseUrl: baseUrl };
}
