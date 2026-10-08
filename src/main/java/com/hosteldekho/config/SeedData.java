package com.hosteldekho.config;

import com.hosteldekho.entity.*;
import com.hosteldekho.entity.Enums.*;
import com.hosteldekho.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.*;
import java.util.*;

@Configuration
public class SeedData {
 private static final List<String> IMAGES=List.of(
  "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=85"
 );

 @Bean CommandLineRunner seed(ManagerRepository mr,CollegeRepository cr,HostelRepository hr,RoomRepository rr,MessMenuItemRepository menuRepository,PasswordEncoder pe){return a->{
  Manager one=upsert(mr,pe,DemoAccounts.FIRST_EMAIL,"priya@hosteldekho.test","Manager One","9876543210");
  Manager two=upsert(mr,pe,DemoAccounts.SECOND_EMAIL,"arjun@hosteldekho.test","Manager Two","9876543211");
  if(hr.count()>0){backfillImages(hr);backfillSlugs(cr);backfillCharges(hr);backfillFood(hr,menuRepository);backfillAvailability(rr);return;}
  College au=college(cr,"Andhra University","Jodimatla",17.728,83.321);
  College gitam=college(cr,"GITAM University","Rushikonda",17.782,83.365);
  College vignan=college(cr,"Vignan Institute of Technology","Duvvada",17.700,83.154);
  College gayatri=college(cr,"Gayatri Vidya Parishad","Madhurawada",17.815,83.340);
  add(hr,rr,one,"Campus Nest Boys Hostel","Comfortable study-friendly boys hostel","Jodimatla",GenderType.MALE,false,au,.7,9000,0);
  add(hr,rr,one,"Srinivas Co-Living","Modern shared living near Andhra University","Jodimatla",GenderType.CO_ED,true,au,1.2,10500,1);
  add(hr,rr,one,"Sea View Girls Residence","Secure girls residence near GITAM","Rushikonda",GenderType.FEMALE,false,gitam,.9,11000,2);
  add(hr,rr,two,"Tech Park Stay","Affordable rooms near Vignan","Duvvada",GenderType.MALE,true,vignan,1.1,7500,3);
  add(hr,rr,two,"Green Leaf Ladies Hostel","Calm female-only accommodation","Madhurawada",GenderType.FEMALE,false,gayatri,.6,9500,4);
  add(hr,rr,two,"City Connect Hostel","Well connected student accommodation","Madhurawada",GenderType.CO_ED,true,gayatri,1.5,8500,5);
  backfillSlugs(cr);
  backfillCharges(hr);
  backfillFood(hr,menuRepository);
  backfillAvailability(rr);
 };}

 private void backfillCharges(HostelRepository repository){
  Map<String,Integer> deposits=Map.of("Campus Nest Boys Hostel",6000,"Srinivas Co-Living",7000,"Sea View Girls Residence",8000,"Tech Park Stay",4000,"Green Leaf Ladies Hostel",6000,"City Connect Hostel",5000);
  List<Hostel> all=repository.findAll();
  for(Hostel h:all)if(deposits.containsKey(h.getName())){
   if(h.getSecurityDeposit()==null)h.setSecurityDeposit(BigDecimal.valueOf(deposits.get(h.getName())));
   if(h.getMaintenanceChargePerMonth()==null)h.setMaintenanceChargePerMonth(BigDecimal.valueOf(500));
   if(h.getMessChargePerMonth()==null)h.setMessChargePerMonth(BigDecimal.valueOf(2500));
   if(h.getElectricityPolicy()==null)h.setElectricityPolicy(ElectricityPolicy.FIXED);
   if(h.getElectricityChargePerMonth()==null)h.setElectricityChargePerMonth(BigDecimal.valueOf(400));
   if(h.getLockInMonths()==null)h.setLockInMonths(3);
   if(h.getNoticePeriodDays()==null)h.setNoticePeriodDays(30);
   if(h.getMessIncludedInRent()==null)h.setMessIncludedInRent(false);
  }
  repository.saveAll(all);
 }

 private void backfillFood(HostelRepository repository,MessMenuItemRepository menuRepository){
  List<Hostel> all=repository.findAll();
  for(Hostel h:all){
   if(!List.of("Campus Nest Boys Hostel","Srinivas Co-Living","Sea View Girls Residence","Tech Park Stay","Green Leaf Ladies Hostel","City Connect Hostel").contains(h.getName()))continue;
   if(h.getFoodType()==null)h.setFoodType(h.getName().contains("Green Leaf")?FoodType.VEG:FoodType.BOTH);
   if(h.getMessIncludedInRent()==null)h.setMessIncludedInRent(false);
   if(h.getMealTimings()==null)h.setMealTimings("Breakfast 7:30–9:00 · Lunch 12:30–14:00 · Dinner 19:30–21:00");
   if(menuRepository.findByHostelIdOrderByDayOfWeek(h.getId()).isEmpty())for(java.time.DayOfWeek day:java.time.DayOfWeek.values())menuRepository.save(MessMenuItem.builder().hostel(h).dayOfWeek(day).breakfast("Idli, fruit and tea").lunch(day==java.time.DayOfWeek.SUNDAY?"Veg biryani and raita":"Rice, dal and seasonal curry").snacks("Tea and biscuits").dinner("Chapati, rice and curry").build());
  }
  repository.saveAll(all);
 }

 private void backfillAvailability(RoomRepository repository){
  java.time.Instant migrationTime=java.time.Instant.now();List<Room> all=repository.findAll();
  for(Room room:all)if(room.getAvailabilityUpdatedAt()==null)room.setAvailabilityUpdatedAt(migrationTime);
  repository.saveAll(all);
 }

 private void backfillSlugs(CollegeRepository repository){
  Set<String> used=new HashSet<>();
  List<College> all=repository.findAll().stream().sorted(Comparator.comparing(College::getId)).toList();
  for(College college:all){
   String slug=college.getSlug();
   if(slug==null||slug.isBlank()){
    String base=college.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$","");
    if(base.isBlank())base="college";
    slug=base;int suffix=2;while(used.contains(slug)||repository.existsBySlug(slug))slug=base+"-"+suffix++;
    college.setSlug(slug);
   }
   used.add(slug);
  }
  repository.saveAllAndFlush(all);
 }

 private Manager upsert(ManagerRepository repository,PasswordEncoder encoder,String email,String legacyEmail,String name,String phone){
  Manager manager=repository.findByEmailIgnoreCase(email).or(()->repository.findByEmailIgnoreCase(legacyEmail)).orElseGet(Manager::new);
  manager.setEmail(email);manager.setName(name);manager.setPhone(phone);manager.setPasswordHash(encoder.encode(DemoAccounts.PASSWORD));return repository.save(manager);
 }
 private College college(CollegeRepository repository,String name,String locality,double latitude,double longitude){
  return repository.findAll().stream().filter(c->c.getName().equalsIgnoreCase(name)).findFirst().orElseGet(()->repository.save(College.builder().name(name).locality(locality).city("Visakhapatnam").latitude(latitude).longitude(longitude).build()));
 }

 private void backfillImages(HostelRepository hr){List<Hostel> all=hr.findAllWithImages();for(int i=0;i<all.size();i++)if(all.get(i).getImageUrls()==null||all.get(i).getImageUrls().isEmpty())all.get(i).setImageUrls(images(i));hr.saveAll(all);}
 private List<String> images(int index){return List.of(IMAGES.get(index%IMAGES.size()),IMAGES.get((index+3)%IMAGES.size()),IMAGES.get((index+6)%IMAGES.size()));}
 private void add(HostelRepository hr,RoomRepository rr,Manager m,String n,String d,String locality,GenderType g,boolean co,College c,double dist,int price,int imageIndex){
  Hostel h=Hostel.builder().name(n).description(d).address("Student Road, "+locality).locality(locality).city("Visakhapatnam").latitude(c.getLatitude()+.005).longitude(c.getLongitude()+.005).genderType(g).coLiving(co).contactName(m.getName()).contactPhone(m.getPhone()).contactEmail(m.getEmail()).rules("No smoking. Visitors only in common areas. Maintain quiet hours after 10 PM.").amenities("WiFi, Laundry, Mess, CCTV, Power Backup").imageUrls(images(imageIndex)).manager(m).build();
  h.getCollegeLinks().add(HostelCollege.builder().hostel(h).college(c).distanceKm(dist).build());h=hr.save(h);
  for(int i=1;i<=3;i++)rr.save(Room.builder().hostel(h).roomNumber("R-"+(100+i)).acType(i==1?AcType.AC:AcType.NON_AC).capacity(i==3?3:2).occupied(i==2?1:0).pricePerMonth(BigDecimal.valueOf(price+(i*1000))).pricePerDay(BigDecimal.valueOf(400+(i*50))).build());
 }
}
