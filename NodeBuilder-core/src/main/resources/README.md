# Node Builder

ARC 솔루션 구성을 위한 Node 어플리케이션 작성 framework

## Requirements

* Java 1.8
* 컴파일 시 -parameters 옵션 필요

## Node Builder Core

Node 어플리케이션 작성을 위한 Core 모듈로, 다음과 같은 기능을 포함한다.

* @Component를 통한 D/I 구현
* singleton, instant 등의 객체 라이프사이클 관리
* @Bind를 통한 D/I 수행

### 기동

##### 고유 메인 클래스 내에서 기동

```
com.epozen.framework.NodeBuilder.main(String[] args);
```

##### JVM 기반으로 기동

```
java com.epozen.framework.NodeBuilder
```

### 필수 설정

Node는 기동 시 JVM 설정을 통한 초기 입력값 설정 필요

ex)

```
java com.epozen.framework.NodeBuilder -Dnode.app.name=Node01_ES_writer01 -Dnode.scan=com.epozen.app -Dnode.app.type=daemon
```

### 기동 옵션 설명

* ***node.app.name** : Node 어플리케이션의 고유한 이름으로 사용할 명칭 지정 (클러스터 기능으로 기동 시 중복 허용되지 않음)
* **node.scan** : @Component 검색을 수행할 최상위 패키지명 지정
* **node.path.conf** : Node 기동 시 설정값을 지정한 xml 파일의 위치를 절대경로 형태로 지정
* **node.app.type** : daemon/fire의 두 가지 앱 타입. daemon의 경우 프로그램 수행 후 기동 상태로 대기하며, fire의 경우 한 번 수행 후 종료됨

**Caution1** : *node.scan*, *node.path.conf*는 동시에 지정할 경우 *node.scan*의 설정을 우선시 하므로 *node.path.conf* 내의 기타 설정들이 무시됨
**Caution2** : *node.scan*, *node.path.conf*가 모두 없을 경우 classpath 바로 아래의 *node-conf.xml* 파일을 검색하여 설정으로 사용
**Caution3** : *node.path.conf*를 설정하지 않고 *node.scan*만을 이용하여 기동하는 경우, 반드시 node.app.type을 지정해야 함 (설정 파일을 사용하는 경우, 따로 지정하지 않으면 daemon 모드로 실행)

## Node Builder Cluster

Node Core에 Data Cluster기능을 활성화. Core와 결합 시 각 설정 데이터 및 유저가 지정한 데이터를 Cluster로 공유하는 역할을 함.

* @Shared를 통해 @Component 내의 필드 데이터를 자동으로 읽어들여 Cluster 내에 id값을 키로 하여 업로드
* lookup=true로 설정할 경우 주기적으로 해당 데이터를 읽어 자동으로 Cluster에 업데이트

## Node Builder Web

Node Core에 REST + Webapp 기능을 활성화. REST end-point를 제공하여 각 설정 데이터 및 클러스터 데이터를 제공

* @RestBinder 설정을 통해 @Component 내의 특정 메서드 정보를 읽어들여 자동으로 REST API로 바인딩
* /api 를 통해 전체 REST API 확인 가능
* /node 를 통해 webapp page에 접근 가능
* ***node.app.name** 에서 설정한 기본 명칭으로 접근 시 전체 설정 정보 확인 가능

## TODO

* Cluster data lookup 기능
* Webapp page application 개발

## Authors

* **[신건호](shingh@jws.com)**
