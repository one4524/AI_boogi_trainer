# AI_boogi_trainer

1. 작품개요

 Deep Learning을 이용한 운동 자세 교정 및 식단 관리 어플리케이션

2. 작품 소개내용

 사용자가 건강 관리를 올바르게 할 수 있도록, 운동자세 교정과 식습관 관리 서비스를 사용자에게 Andriod App을 통해 제공한다. 운동자세 교정은 스마트폰 카메라로부터 들어오는 실시간 이미지를, 사람의 포즈를 인식하는 MoveNet - 다중분류 모델에 적용하여 운동 자세를 인식하면 올바른 자세인지, 해당 자세를 몇 번 움직이는지, 해당 운동으로 몇 kcal가 소모되는지 등의 정보를 사용자에게 제공한다. 식습관 관리는 사용자가 찍은 음식 이미지를 YOLO-V5 모델에 적용하여, 음식의 종류를 파악하고 해당 음식의 영양성분과 kcal를 계산하여, 건강한 식습관을 형성할 수 있도록 사용자 맞춤형 추천 식단을 제공한다.
 
3. 기대효과
 
 운동케어 기능을 통해 사용자의 운동 자세를 인식하여 사용자에게 자세가 정확한지 알려준다. 이러한 과정을 통해 사용자의 운동 자세를 교정하여 혼자서 하는 운동의 부상을 예방할 수 있다. 또한, 운동의 종류와 횟수, 시간, 소모한 kcal를 자동으로 기록하여 체계적인 관리와 효율적인 운동이 가능할 것이다. 식단관리 기능을 통해 사용자의 식습관을 한 눈에 파악할 수 있고, 식단 추천 및 관리 기능을 통해 균형 잡힌 식습관을 형성하도록 도움을 받을 수 있다. 운동 및 식단 기록을 통해 건강관리 계획을 체계적으로 세울 수 있다.  

4. 주요 적용 기술 및 구조
<img width="819" alt="스크린샷 2022-05-27 오전 4 02 31" src="https://user-images.githubusercontent.com/86751964/170558563-3bb7b121-e473-401d-a883-6c3d30fb289d.png">

-작품 소개 사진
![1](https://user-images.githubusercontent.com/86751964/171995542-25dec6d8-f327-41d2-bdbf-cf4964b4cfb8.png)

![2](https://user-images.githubusercontent.com/86751964/171995554-de13227b-1284-43bb-8b40-b457bf70f6d5.png)
- 홈 화면에서 오늘 섭취한 칼로리를 소모하려면 유산소 운동을 얼마나 해야 하는지 알려준다.
- 홈 화면에서 일일 권장 섭취 탄수화물, 단백질, 지방을 섭취하려면 어떤 음식을 얼마나 먹어야 한느지 알려준다.
- 식단 관리 화면에서 사용자가 기록한 음식 정보를 바탕으로 일일 권장 섭취량과 비교해 얼마나 섭취하였는지 알려준다. 아침, 점심, 저녁 중 하나를 선택하면 음식을 촬영할 수 있는 화면으로 이동한다.

![3](https://user-images.githubusercontent.com/86751964/171995581-acb6d497-2b67-499b-86a2-9da905554a17.png)
- 사용자가 사진 촬영이 불가능한 경우 음식을 검색하거나 데이터베이스에 해당 음식이 없을 경우 음식을 직접 등록할 수 있다.
- 음식 사진을 촬영한 경우 음식을 인식하고 사용자가 용량을 입력하고 저장하면 칼로리, 단백질, 탄수화물, 지방의 정보가 식단 관리 화면에 반영된다.

![4](https://user-images.githubusercontent.com/86751964/171995600-be7b7751-bfe4-4648-bbf8-deeb53cae75e.png)
- 운동 선택 화면에서 9가지 종류의 운동을 선택할 수 있다.
- 유산소 조깅의 경우 지도에서 이동 경로와 거리를 확인할 수 있다.

![5](https://user-images.githubusercontent.com/86751964/171995623-88968c14-2288-4570-8a14-155a23ebf1da.png)
- 근력 운동의 경우 관절 좌표를 통해 자세를 인식하고 틀린 자세는 tts를 통해 사용자에게 알려준다.
- 마이페이지의 달력에서 날짜를 선택하면 그 날의 식단과 운동 정보를 요약해 보여준다.

![스캔된 문서1024_1](https://user-images.githubusercontent.com/86751964/191588659-b7319287-39f7-4faa-9b41-159fe496f9c2.jpg)

 
