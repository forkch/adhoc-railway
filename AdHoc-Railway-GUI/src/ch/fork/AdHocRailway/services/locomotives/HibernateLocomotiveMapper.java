package ch.fork.AdHocRailway.services.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class HibernateLocomotiveMapper {

	public static Locomotive mapLocomotive(
			HibernateLocomotive hibernateLocomotive) {

		Locomotive locomotive = new Locomotive();
		locomotive.setName(hibernateLocomotive.getName());
		locomotive.setId(hibernateLocomotive.getId());
		locomotive.setBus(hibernateLocomotive.getBus());
		locomotive.setDescription(hibernateLocomotive.getDescription());
		locomotive.setImage(hibernateLocomotive.getImage());
		locomotive.setAddress(hibernateLocomotive.getAddress());
		locomotive.setLocomotiveGroupId(hibernateLocomotive
				.getLocomotiveGroup().getId());
		locomotive.setLocomotiveTypeId(hibernateLocomotive.getLocomotiveType()
				.getId());

		return locomotive;
	}

	public static LocomotiveGroup mapLocomotiveGroup(
			HibernateLocomotiveGroup hLocomotiveGroup) {
		LocomotiveGroup group = new LocomotiveGroup();
		group.setId(hLocomotiveGroup.getId());
		group.setName(hLocomotiveGroup.getName());
		return group;
	}

	public static LocomotiveType mapLocomotiveType(
			HibernateLocomotiveType hLocomotiveType) {
		LocomotiveType type = new LocomotiveType();
		type.setId(hLocomotiveType.getId());
		type.setDrivingSteps(hLocomotiveType.getDrivingSteps());
		type.setFunctionCount(hLocomotiveType.getFunctionCount());
		type.setStepping(hLocomotiveType.getStepping());
		type.setTypeName(hLocomotiveType.getTypeName());
		return type;
	}

}
