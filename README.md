# Lab 8: Mockito mock объектууд

Нэр: Tuguldur
Код: B242270050

## java -version
```
java version "25.0.2" 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 25.0.2+10-LTS-69)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.2+10-LTS-69, mixed mode, sharing)
```

## mvn -version
```
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Maven home: /opt/homebrew/Cellar/maven/3.10.0/libexec
Java version: 27.0.1.0.3, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/27.0.1.0.3/libexec/openjdk.jdk/Contents/Home
Default locale: en_MN, platform encoding: UTF-8, time zone: Asia/Ulaanbaatar
OS name: "mac os x", version: "26.0", arch: "aarch64", family: "mac"
```

## Тестийн үр дүн

`results/mvn-test.txt`-ийн сүүлийн мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, BUILD SUCCESS.
(7 энгийн тест + A8-ийн 3 мөр.)

## A8 дахь lenient()

A8 параметржүүлсэн тест нь мөр бүрдээ өөр stub тохируулдаг (жишээ нь NO_STUDENT мөрөнд `courseExists`-ийн stub огт хэрэггүй). MockitoExtension-ий strict stubs горим ашиглагдаагүй stub-д UnnecessaryStubbingException өгдөг тул `lenient().when(...)` хэрэглэж, зөвхөн тухайн мөрөнд хэрэгтэй stub-уудыг тохируулсан.

## Мутацийн тест (Хэсэг B)

| Мутант | Эвдсэн зүйл | Унасан тестүүд | Файл |
|---|---|---|---|
| 1 | Идэвхгүй оюутны шалгалтыг комментлов | `register_inactiveStudent_returnsErrorInactiveAndNeverTouchesEmail` (A3), `register_errorScenarios_returnExpectedErrorCode[2]` (A8, INACTIVE_STUDENT) | results/mvn-test-mutant-1.txt |
| 2 | `saveRegistration(...)` дуудалтыг комментлов | `register_activeStudentWithAllPrerequisites_returnsOkSavesAndSendsEmail` (A1), `register_emailSenderThrows_stillReturnsOkAndRegistrationSaved` (A5) | results/mvn-test-mutant-2.txt |
| 3 | `\|\| courseId == null`-ийг устгав | `register_nullArguments_throwsIllegalArgumentAndTouchesNoDependencies` (A6) | results/mvn-test-mutant-3.txt |

Гурван мутант бүгд баригдсан (тус бүрд BUILD FAILURE), тэгэхээр тестэд цоорхой олдсонгүй. A6 нь `register(null, ...)` ба `register(..., null)` хоёрыг тусад нь шалгадаг тул мутант 3-ыг барьсан.


## Дүгнэлт

Mock бол гадаад хамаарлыг (өгөгдлийн сан, и-мэйл илгээгч) орлуулж, түүнд хийгдсэн дуудалтыг бүртгэн, урьдчилан тохируулсан хариу буцаадаг объект юм. Fake бол жинхэнэ ажиллагаатай хөнгөн хэрэгжүүлэлт (жишээ нь санах ойн Map-д суурилсан repository) бөгөөд дуудалтыг биш төлөвийг шалгахад тохиромжтой. Энэ лабд emailSender-ийг mock хийсэн нь зөв байсан, учир нь "и-мэйл явсан уу, биед нь хичээлийн код орсон уу" гэдэг нь зан төлөв бөгөөд үүнийг verify болон ArgumentCaptor-оор шалгасан. Тестлэгдэж буй CourseRegistrationService-ийг өөрийг нь mock хийж болохгүй, учир нь бодит логик ажиллахгүй болж юуг ч шалгахгүй. Student шиг энгийн өгөгдлийн record-ийг ч mock хийх хэрэггүй, шууд new Student(...) гэж үүсгэх нь илүү энгийн бөгөөд бодит юм. verify-г хэтрүүлбэл тест кодын дотоод хэрэгжилт (дуудалтын дараалал, тоо)-той хэт нягт холбогдож, үр дүн нь зөв хэвээр байхад рефакторингийн үед тест унадаг болно. Тиймээс A2-ийн never() шиг бизнесийн дүрмийг илэрхийлж буй verify-г л хэрэглэх нь зөв. Мутацийн тестээр гурван мутант бүгд баригдсан бөгөөд мутант 3-ыг A6 барьсан нь null аргумент бүрийг тусад нь шалгах нь тестэд чухал болохыг харуулсан.
