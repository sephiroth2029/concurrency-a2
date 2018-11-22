call mvn clean package
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-0 --concurrency.a2.byz.id=0 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=true --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-0.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-1 --concurrency.a2.byz.id=1 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-1.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-2 --concurrency.a2.byz.id=2 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=true --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-2.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-3 --concurrency.a2.byz.id=3 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-3.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-4 --concurrency.a2.byz.id=4 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-4.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-5 --concurrency.a2.byz.id=5 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=true --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-5.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-6 --concurrency.a2.byz.id=6 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-6.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-7 --concurrency.a2.byz.id=7 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-7.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-8 --concurrency.a2.byz.id=8 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-8.log --spring.profiles.active=chaos-monkey
start java -Xmx512m -jar target/byzantine-gens-0.0.1-SNAPSHOT.jar --spring.application.name=general-9 --concurrency.a2.byz.id=9 --concurrency.a2.byz.total-generals=10 --concurrency.a2.byz.traitor=false --concurrency.a2.byz.rounds=5 --logging.file=logs/byzantine-generals-9.log --spring.profiles.active=chaos-monkey