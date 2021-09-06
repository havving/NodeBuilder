# NodeBuilder

ARC 솔루션 구성을 위한 Node Application 작성 framework

## Requirements

* Java 1.8
* 컴파일 시 -parameters 옵션 필요

## NodeBuilder Core

Node Application 작성을 위한 Core 모듈로, 다음과 같은 기능을 포함한다.

* @Component를 통한 D/I 구현
* singleton, instant 등의 객체 라이프사이클 관리
* @Bind를 통한 D/I 수행

### 기동

##### 고유 메인 클래스 내에서 기동

```
com.havving.framework.NodeBuilder.main(String[] args);
```

##### JVM 기반으로 기동

```
java com.havving.framework.NodeBuilder
```

### 필수 설정

Node는 기동 시 JVM 설정을 통한 초기 입력값 설정 필요

ex)

```
java com.havving.framework.NodeBuilder -Dnode.app.name=Node01 -Dnode.scan=com.havving.app -Dnode.app.type=daemon
```

### 기동 옵션 설명

* ***node.app.name** : Node 어플리케이션의 고유한 이름으로 사용할 명칭 지정 (클러스터 기능으로 기동 시 중복 허용되지 않음)
* **node.scan** : @Component 검색을 수행할 최상위 패키지명 지정
* **node.path.conf** : Node 기동 시 설정값을 지정한 xml 파일의 위치를 절대경로 형태로 지정
* **node.app.type** : daemon/fire의 두 가지 앱 타입. daemon의 경우 프로그램 수행 후 기동 상태로 대기하며, fire의 경우 한 번 수행 후 종료됨

**Caution1** : *node.scan*, *node.path.conf*는 동시에 지정할 경우 *node.scan*의 설정을 우선시 하므로 *node.path.conf* 내의 기타 설정들이 무시됨<br>
**Caution2** : *node.scan*, *node.path.conf*가 모두 없을 경우 classpath 바로 아래의 *node-conf.xml* 파일을 검색하여 설정으로 사용<br>
**Caution3** : *node.path.conf*를 설정하지 않고 *node.scan*만을 이용하여 기동하는 경우, 반드시 node.app.type을 지정해야 함 (설정 파일을 사용하는 경우, 따로 지정하지 않으면 daemon 모드로 실행)

## Node Builder Cluster

Cluster 모듈을 통한 프로세스/서버 간 데이터 공유 및 조회 기능 제공

* Node Core에 Data Cluster 기능을 활성화하여 Core와 결합 시, 각 설정 데이터 및 유저가 지정한 데이터를 Cluster로 공유
* @Shared를 통해 @Component 내의 필드 데이터를 자동으로 읽어들여 Cluster 내에 id값을 키로 하여 업로드
* lookup=true로 설정할 경우 주기적으로 해당 데이터를 읽어 자동으로 Cluster에 업데이트

## NodeBuilder Core

Core 모듈을 통한 D/I(Dependency Injection) 기능 구현

* @Component 어노테이션을 이용하여 클래스 자동 생성
* @Bind 어노테이션을 이용하여 @Component 클래스 내에서 Dependency Injection 자동화 
* Factory를 이용하여 각 오브젝트 관리
* 기동 시 특정 위치의 설정파일을 읽어들여 전체 프로세스에 적용
* 프로세스 설정정보 조회 및 변경기능 제공


## NodeBuilder Web

Node Core에 REST + WebApp 기능을 활성화. REST end-point를 제공하여 각 설정 데이터 및 클러스터 데이터를 제공

* @RestBinder 설정을 통해 @Component 내의 특정 메서드 정보를 읽어들여 자동으로 REST API로 바인딩
* /api 를 통해 전체 REST API 확인 가능
* /node 를 통해 webapp page에 접근 가능
* ***node.app.name** 에서 설정한 기본 명칭으로 접근 시 전체 설정 정보 확인 가능

## NodeBuilder RV

RV 모듈을 통한 RV Connection 생성 및 관리 단순화

* RV 및 RVDriver 관련 기능 제공
* Transport 생성 및 메시지 송/수신 관련 기능 제공  
* RV Connection 검증 후 연결
* 설정/작업 정보들을 Cluster 공유

## NodeBuilder Scheduler

Scheduler 모듈을 통한 특정 로직의 시간 별 수행 기능 제공

* @Component로 지정한 Serializable 클래스 내의 메서드에 크론 표현식을 적용한 @ReservedTarget 어노테이션을 지정, 지정된 시간마다 해당 메서드를 실행토록 함
* Quartz 기능 제공
* RAM / DB 타입 배치 기능 제공
* 설정/작업 정보들을 Cluster 공유
