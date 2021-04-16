# Node Builder

Node Application 작성 framework

## Requirements

* Java 1.8
* 컴파일 시, -parameters 옵션 필요

## Node Builder - Core

Node Application 작성을 위한 Core 모듈로, 다음과 같은 기능을 포함한다.

* @Component를 통한 D/I 구현
* @Bind를 통한 D/I 수행
* singleton, instant 등의 객체 Life-Cycle 관리

### 기동

##### 고유 메인 클래스 내에서 기동

```
com.havving.framework.NodeBuilder.main(String[] args);
```

##### JVM 기반으로 기동

```
java com.havving.framework.NodeBuilder
```

